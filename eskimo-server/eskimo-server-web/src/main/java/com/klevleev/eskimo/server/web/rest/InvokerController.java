package com.klevleev.eskimo.server.web.rest;

import com.klevleev.eskimo.invoker.domain.InvokerNodeInfo;
import com.klevleev.eskimo.server.core.judge.JudgeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.net.URISyntaxException;

/**
 * Created by Stepan Klevleev on 16-Aug-16.
 */
@RestController
public class InvokerController {

	private static final Logger logger = LoggerFactory.getLogger(InvokerController.class);

	private final JudgeService judgeService;

	@Autowired
	public InvokerController(JudgeService judgeService) {
		this.judgeService = judgeService;
	}

	@RequestMapping(value = "/invoker/register", method = RequestMethod.POST)
	public Boolean register(InvokerNodeInfo invokerNodeInfo) {
		try {
			judgeService.registerInvoker(invokerNodeInfo);
		} catch (URISyntaxException e) {
			logger.error("can not register invoker: " + invokerNodeInfo, e);
			return false;
		}
		return true;
	}
}