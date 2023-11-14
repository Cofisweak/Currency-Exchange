package org.cofisweak.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.cofisweak.dto.ExchangeRateDto;
import org.cofisweak.exception.*;
import org.cofisweak.model.ExchangeRate;
import org.cofisweak.service.ExchangeService;
import org.cofisweak.util.ResponseBuilder;

import java.io.IOException;
import java.util.Optional;

@WebServlet("/exchangeRate/*")
public class ExchangeRateServlet extends HttpServlet {
    private static final ExchangeService exchangeService = ExchangeService.getInstance();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String requestURI = req.getRequestURI();
        System.out.println(requestURI);
        String code = requestURI.substring(requestURI.lastIndexOf("/") + 1);
        try {
            Optional<ExchangeRate> exchangeRate = exchangeService.getExchangeRateByCurrencyCodePair(code);
            if (exchangeRate.isEmpty()) {
                throw new ExchangeRateNotFoundException();
            } else {
                ExchangeRateDto dto = exchangeService.getExchangeRateDto(exchangeRate.get());
                ResponseBuilder.writeResultToResponse(dto, resp);
            }
        } catch (DaoException e) {
            resp.setStatus(500);
            ResponseBuilder.writeErrorToResponse(e.getMessage(), resp);
        } catch (ExchangeRateNotFoundException | InvalidCurrencyCodeException | InvalidCurrencyCodePairException e) {
            resp.setStatus(404);
            ResponseBuilder.writeErrorToResponse(e.getMessage(), resp);
        }

    }

    protected void doPatch(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String requestURI = req.getRequestURI();
        String code = requestURI.substring(requestURI.lastIndexOf("/") + 1);
        try {
            String rate = getRateFromRequestBody(req);
            ExchangeRateDto dto = exchangeService.updateRateOfExchangeRateByCurrencyCodePair(code, rate);
            ResponseBuilder.writeResultToResponse(dto, resp);
        } catch (DaoException e) {
            resp.setStatus(500);
            ResponseBuilder.writeErrorToResponse(e.getMessage(), resp);
        } catch (InvalidCurrencyCodeException | ExchangeRateNotFoundException | InvalidCurrencyCodePairException e) {
            resp.setStatus(404);
            ResponseBuilder.writeErrorToResponse(e.getMessage(), resp);
        } catch (MissingFieldException | IllegalRateException e) {
            resp.setStatus(400);
            ResponseBuilder.writeErrorToResponse(e.getMessage(), resp);
        }
    }

    private String getRateFromRequestBody(HttpServletRequest req) throws IOException, MissingFieldException {
        String parameters = req.getReader().readLine();
        if (parameters == null) {
            throw new MissingFieldException("rate");
        }
        String[] parameterEntry = parameters.split("=");
        if (parameters.length() < 2 || !parameterEntry[0].equalsIgnoreCase("rate")) {
            throw new MissingFieldException("rate");
        }
        return parameterEntry[1];
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String method = req.getMethod();
        if (!method.equals("PATCH")) {
            super.service(req, resp);
        } else {
            this.doPatch(req, resp);
        }
    }
}
