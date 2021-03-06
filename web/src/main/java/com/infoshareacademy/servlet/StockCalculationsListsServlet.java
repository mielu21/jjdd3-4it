package com.infoshareacademy.servlet;

import com.infoshareacademy.cdi.AxisStringConverterBean;
import com.infoshareacademy.cdi.StockCalculationsListsBean;
import com.infoshareacademy.dao.CurrencyStatisticDao;
import com.infoshareacademy.dao.InputDataDao;
import com.infoshareacademy.freemarker.TemplateProvider;
import com.infoshareacademy.model.CurrencyStatistic;
import com.infoshareacademy.model.InputData;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/list")
public class StockCalculationsListsServlet extends HttpServlet {

    private static final Logger LOG = LoggerFactory.getLogger(StockCalculationsListsServlet.class);

    @Inject
    private StockCalculationsListsBean countingFunctionBean;
    @Inject
    private InputDataDao inputDataDao;
    @Inject
    private AxisStringConverterBean axisStringConverterBean;
    @Inject
    private CurrencyStatisticDao currencyStatisticDao;

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        LocalDate startDate = LocalDate.parse(req.getParameter("start"));
        LocalDate endDate = LocalDate.parse(req.getParameter("end"));
        String currencyName = req.getParameter("cryptoCurrency").toLowerCase();
        String pathToFile = getServletContext().getResource("/WEB-INF/currency/" + currencyName + ".csv").getPath();
        LOG.info("Path to file:  {}", pathToFile);

        saveInputDataToDataBase(currencyName, pathToFile);
        addCurencyCountValueToStatisticDatabase(currencyName);

        LOG.info("start counting min, max, avg. med");
        List<InputData> sortCryptoData = countingFunctionBean.sortDataByBean(currencyName, startDate, endDate);
        InputData minPrice = countingFunctionBean.printMinPriceBean(currencyName, startDate, endDate);
        InputData maxPrice = countingFunctionBean.printMaxPriceBean(currencyName, startDate, endDate);
        Double averageOfPrice = countingFunctionBean.avaragePriceForRangeBean(currencyName, startDate, endDate);
        Double medianOfPrice = countingFunctionBean.medianPriceForRangeBean(currencyName, startDate, endDate);
        String axisX = axisStringConverterBean.axisX(sortCryptoData);
        String axisY = axisStringConverterBean.axisY(sortCryptoData);

        Map<String, Object> dataModel = new HashMap<>();
        dataModel.put("min", minPrice);
        dataModel.put("max", maxPrice);
        dataModel.put("avg", averageOfPrice);
        dataModel.put("med", medianOfPrice);
        dataModel.put("startdate", startDate);
        dataModel.put("enddate", endDate);
        dataModel.put("cryptoCurrency", currencyName);
        dataModel.put("axisX", axisX);
        dataModel.put("axisY", axisY);
        Template template = TemplateProvider.createTemplate(getServletContext(), "start-menu.ftlh");

        try {
            template.process(dataModel, resp.getWriter());
        } catch (TemplateException e) {
            LOG.error("Error template loading: {}", e);
        }
    }

    private void saveInputDataToDataBase(String currencyName, String pathToFile) throws IOException {
        LOG.info("Save data with currencies to database");
        List<InputData> printData = countingFunctionBean.readFileBean(pathToFile);
        LOG.info("Data add to data base from currency {}", currencyName);
        for (InputData inputDataDb : printData) {
            inputDataDb.setCurrency(currencyName);
            inputDataDao.save(inputDataDb);
        }
    }

    private void saveStatisticToDataBase(String currencyName) {
        currencyStatisticDao.save(new CurrencyStatistic(currencyName, 1));
    }

    private void addCurencyCountValueToStatisticDatabase(String currencyName) {
        LOG.info("Save statistic to database");
        CurrencyStatistic currencyStatistic = currencyStatisticDao.findStatisticByCurrency(currencyName);
        if (currencyStatistic == null) {
            saveStatisticToDataBase(currencyName);
        } else {
            Integer currencyValue = currencyStatistic.getValue();
            currencyStatistic.setValue(currencyValue + 1);
            currencyStatisticDao.update(currencyStatistic);
        }
    }

}
