package com.klevleev.eskimo.server.web.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Locale;

@Controller
public class HomeController {

	private static final Logger logger = LoggerFactory.getLogger(HomeController.class);

	@RequestMapping(value = "/", method = RequestMethod.GET)
	public String welcome(ModelMap model, Locale locale) {
		String lan = locale.getLanguage();
		return "home";
	}

	@RequestMapping(value = "/login", method = RequestMethod.GET)
	public String login(@RequestParam(value = "error", required = false) String error,
	                    @RequestParam(value = "logout", required = false) String logout,
	                    ModelMap model,
	                    Authentication authentication) {
		if (authentication != null) {
			return "redirect:/";
		}
		if (error != null) {
			model.addAttribute("error", "Invalid username or password!");
		}
		if (logout != null) {
			model.addAttribute("msg", "You've been logged out successfully.");
		}
		return "login";
	}

	@RequestMapping(value = "/signup", method = RequestMethod.GET)
	public String login(ModelMap model,
	                    Authentication authentication) {
		if (authentication != null) {
			return "redirect:/";
		}
		return "signup";
	}

}