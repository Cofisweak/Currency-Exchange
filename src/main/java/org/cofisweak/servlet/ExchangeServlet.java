package org.cofisweak.servlet;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.cofisweak.dto.ExchangeRequestDto;
import org.cofisweak.service.ExchangeService;

import java.io.IOException;

@WebServlet("/exchange")
public class ExchangeServlet extends HttpServlet {
    private static final ExchangeService exchangeService = ExchangeService.getInstance();
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        ExchangeRequestDto requestDto = new ExchangeRequestDto(
                req.getParameter("from"),
                req.getParameter("to"),
                req.getParameter("amount"));
    }
}
