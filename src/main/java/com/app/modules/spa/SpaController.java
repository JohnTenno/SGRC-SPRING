package com.app.modules.spa;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SpaController {

    @GetMapping(value = {
        "/",
        "/{a:[\\w-]+}",
        "/{a:[\\w-]+}/{b:[\\w-]+}",
        "/{a:[\\w-]+}/{b:[\\w-]+}/{c:[\\w-]+}"
    })
    public String forward() {
        return "forward:/index.html";
    }
}
