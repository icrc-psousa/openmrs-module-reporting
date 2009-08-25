package org.openmrs.module.reporting.web.controller.portlet;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.evaluation.BaseDefinition;
import org.openmrs.module.evaluation.parameter.Parameterizable;
import org.openmrs.module.util.ParameterizableUtil;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ParameterizablePortletFormController {

	protected static Log log = LogFactory.getLog(ParameterizablePortletFormController.class);
	
	/**
	 * Default Constructor
	 */
	public ParameterizablePortletFormController() { }
    
    /**
     * Saves a BaseOpenmrsMetadata object
     */
    @RequestMapping("/module/reporting/reports/saveBaseParameterizable")
    public String saveBaseParameterizable(ModelMap model, HttpServletRequest request,
    		@RequestParam(required=true, value="type") Class<? extends BaseDefinition> type,
    		@RequestParam(required=true, value="uuid") String uuid,
    		@RequestParam(required=true, value="name") String name,
    		@RequestParam(required=true, value="description") String description){
    	
    	Parameterizable p = null;
    	if (StringUtils.isNotEmpty(uuid)) {
    		p = ParameterizableUtil.getParameterizable(uuid, type);
    	}
    	else {
    		try {
    			p = type.newInstance();
    		}
    		catch (Exception e) {
    			throw new IllegalArgumentException("Cannot instantiate a new " + type, e);
    		}
    	}
    	p.setName(name);
    	p.setDescription(description);
    	p = ParameterizableUtil.saveParameterizable(p);
    	
    	String successUrl = request.getParameter("successUrl");
    	if (StringUtils.isNotEmpty(successUrl)) {
    		successUrl = "redirect:"+successUrl.replace("=uuid", "=" + p.getUuid());
    		return successUrl;
    	}
    	return "redirect:/module/reporting/closeWindow.htm";
    }
}