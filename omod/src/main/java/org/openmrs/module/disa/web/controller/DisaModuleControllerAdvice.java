package org.openmrs.module.disa.web.controller;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.openmrs.api.context.Context;
import org.openmrs.module.disa.api.exception.DisaModuleAPIException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice("org.openmrs.module.disa")
public class DisaModuleControllerAdvice {

    /*
     * Allow converting empty String to null when binding Dates. Otherwise
     * validation will throw a typeMismatch error.
     */
    @InitBinder
    public void initBinder(WebDataBinder binder) {
        SimpleDateFormat dateFormat = Context.getDateFormat();
        binder.registerCustomEditor(Date.class, new CustomDateEditor(dateFormat, true));
    }
}
