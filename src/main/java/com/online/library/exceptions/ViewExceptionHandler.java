package com.online.library.exceptions;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

@ControllerAdvice(basePackages = "com.online.library.controllers.views")
public class ViewExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ModelAndView handleResourceNotFoundException(ResourceNotFoundException exception) {
        ModelAndView mav = new ModelAndView("error/404");
        mav.addObject("message", exception.getMessage());
        return mav;
    }
}
