package com.infoshareacademy.servlet;

import com.infoshareacademy.cdi.StatisticsStringConvertBean;
import com.infoshareacademy.dao.CurrencyStatisticDao;
import com.infoshareacademy.freemarker.TemplateProvider;
import com.infoshareacademy.model.CurrencyStatistic;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/statistic")
public class CurrencyStatisticServlet extends HttpServlet {

    private static final Logger LOG = LoggerFactory.getLogger(CurrencyStatisticServlet.class);

    @Inject
    CurrencyStatisticDao currencyStatisticDao;
    @Inject
    StatisticsStringConvertBean statisticsStringConvertBean;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        findAllCurrenciesStatistic(req, resp);
    }

    private void findAllCurrenciesStatistic(HttpServletRequest request, HttpServletResponse response) throws IOException {

        List<CurrencyStatistic> currenciesStatistics = currencyStatisticDao.findAll();
        String labels = statisticsStringConvertBean.statsName(currenciesStatistics);
        String values = statisticsStringConvertBean.statsValue(currenciesStatistics);

        LOG.info("Load statistic to screen");

        Map<String, String> dataModel = new HashMap<>();
//        dataModel.put("stats", currenciesStatistics);
        dataModel.put("labels", labels);
        dataModel.put("values", values);

//        for (CurrencyStatistic currencyStatistic : currenciesStatistics) {
//            response.getWriter().println(currencyStatistic);
//        }

        Template template = TemplateProvider.createTemplate(getServletContext(), "statistics.ftlh");

        try {
            template.process(dataModel, response.getWriter());
        } catch (TemplateException e) {
            LOG.error("Error template loading: {}", e);
        }

        }
}
