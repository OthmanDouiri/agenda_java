package com.agenda.agendaespacios.controller;

import org.springframework.boot.autoconfigure.web.WebProperties.LocaleResolver;

import jakarta.servlet.http.HttpServletRequest;

public class RequestContextUtils {

    public static LocaleResolver getLocaleResolver(HttpServletRequest request) {
        throw new UnsupportedOperationException("Unimplemented method 'getLocaleResolver'");
    }

}
