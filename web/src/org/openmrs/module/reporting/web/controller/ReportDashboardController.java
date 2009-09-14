package org.openmrs.module.reporting.web.controller;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Cohort;
import org.openmrs.Program;
import org.openmrs.api.context.Context;
import org.openmrs.module.cohort.definition.AgeCohortDefinition;
import org.openmrs.module.cohort.definition.GenderCohortDefinition;
import org.openmrs.module.cohort.definition.ProgramStateCohortDefinition;
import org.openmrs.module.cohort.definition.service.CohortDefinitionService;
import org.openmrs.module.dataset.DataSet;
import org.openmrs.module.dataset.MapDataSet;
import org.openmrs.module.dataset.column.DataSetColumn;
import org.openmrs.module.dataset.definition.DataSetDefinition;
import org.openmrs.module.dataset.definition.PatientDataSetDefinition;
import org.openmrs.module.dataset.definition.service.DataSetDefinitionService;
import org.openmrs.module.evaluation.EvaluationContext;
import org.openmrs.module.indicator.dimension.CohortIndicatorAndDimensionResult;
import org.openmrs.module.indicator.service.IndicatorService;
import org.openmrs.module.report.ReportData;
import org.openmrs.module.report.ReportDefinition;
import org.openmrs.module.report.service.ReportService;
import org.openmrs.module.reporting.ReportingConstants;
import org.openmrs.module.util.CohortUtil;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ReportDashboardController {

	private Log log = LogFactory.getLog(this.getClass());
	
	/**
	 * Constructor
	 */
	public ReportDashboardController() { }
	
	/**
	 * Registers custom editors for fields of the command class.
	 * 
	 * @param binder
	 */
    @InitBinder
    public void initBinder(WebDataBinder binder) { 
    	binder.registerCustomEditor(Date.class, new CustomDateEditor(Context.getDateFormat(), false)); 
    }    

    
    /**
     * 
     * @param cohort
     * @param model
     * @return
     */
    @RequestMapping("/module/reporting/dashboard/viewCohortDataSet")
    public String viewCohortDataSet(
    		@RequestParam(required=false, value="savedDataSetId") String savedDataSetId,
    		@RequestParam(required=false, value="savedColumnKey") String savedColumnKey,   		
    		@RequestParam(required=false, value="applyDataSetId") String applyDataSetId,
    		@RequestParam(required=false, value="limit") Integer limit,
    		HttpServletRequest request,
    		ModelMap model) { 
    	    
    	
		ReportData reportData = (ReportData) request.getSession().getAttribute(ReportingConstants.OPENMRS_REPORT_DATA);
    
		for (DataSet dataSet : reportData.getDataSets().values()) { 
			if (dataSet.getDefinition().getUuid().equals(savedDataSetId)) { 
				
				MapDataSet mapDataSet = (MapDataSet) dataSet;
				DataSetDefinition definition = (DataSetDefinition) dataSet.getDefinition();
				DataSetColumn dataSetColumn = definition.getColumn(savedColumnKey);
				CohortIndicatorAndDimensionResult result = (CohortIndicatorAndDimensionResult) mapDataSet.getData(dataSetColumn);	
				Cohort selectedCohort = result.getCohort();
				
				model.addAttribute("selectedCohort", selectedCohort);
				model.addAttribute("patients", Context.getPatientSetService().getPatients(selectedCohort.getMemberIds()));		
				
				// Evaluate the default patient dataset definition
				DataSetDefinition dsd = null; 
				try {
					
					dsd = Context.getService(DataSetDefinitionService.class).getDataSetDefinition(applyDataSetId, null);
					
				} catch (Exception e) { 
					log.error("exception getting dataset definition", e);					
				}
				
				if (dsd == null) 
					dsd = new PatientDataSetDefinition();
				
				EvaluationContext evalContext = new EvaluationContext();
				if (limit != null && limit > 0) 
					evalContext.setLimit(limit);
				evalContext.setBaseCohort(selectedCohort);
				
				DataSet patientDataSet = Context.getService(DataSetDefinitionService.class).evaluate(dsd, evalContext);
				model.addAttribute("dataSet", patientDataSet);
		    	model.addAttribute("dataSetDefinition", dsd);
				
			}
		}
    	// Add all dataset definition to the request (allow user to choose)
    	model.addAttribute("dataSetDefinitions", 
    			Context.getService(DataSetDefinitionService.class).getAllDataSetDefinitions(false)); 			
		
    	return "/module/reporting/dashboard/cohortDataSetDashboard";
    	
    }
    
    
    /**
     * 
     * @param cohort
     * @param model
     * @return
     */
    @RequestMapping("/module/reporting/dashboard/manageCohortDashboard")
    public String manageCohortDashboard(
    		@RequestParam(required=false, value="cohort") String cohort,
    		ModelMap model) { 
    	
    	Cohort selectedCohort = null;
		model.addAttribute("selected", cohort);
		
    	EvaluationContext evaluationContext = new EvaluationContext();
    	if ("males".equalsIgnoreCase(cohort)) {
    		selectedCohort = getGenderCohort(evaluationContext, "M");
    	}
    	else if ("females".equalsIgnoreCase(cohort)) { 
    		selectedCohort = getGenderCohort(evaluationContext, "F");    		
    	}
    	else if ("adults".equalsIgnoreCase(cohort)) { 
    		selectedCohort = getAgeCohort(evaluationContext, 15, 150, new Date());    		
    	}
    	else if ("children".equalsIgnoreCase(cohort)) { 
    		selectedCohort = getAgeCohort(evaluationContext, 0, 14, new Date());    		
    	}
    	else if ("all".equalsIgnoreCase(cohort)) { 
    		selectedCohort = Context.getPatientSetService().getAllPatients();
    	}
    	else { 
    		
    		if (cohort != null) { 
	    		Program program = Context.getProgramWorkflowService().getProgramByName(cohort);
	    		if (program != null) 
	    			selectedCohort = getProgramStateCohort(evaluationContext, program);    		
	    		else {  
	    			selectedCohort = CohortUtil.limitCohort(Context.getPatientSetService().getAllPatients(), 100);
	    		}
    		}
    	}
    	if (selectedCohort != null && !selectedCohort.isEmpty()) { 
    		// Evaluate on the fly report
    		/*
    		EvaluationContext evalContext = new EvaluationContext();
    		evalContext.setBaseCohort(selectedCohort);
	    	ReportDefinition reportDefinition = new ReportDefinition();
	    	DataSetDefinition dataSetDefinition = new PatientDataSetDefinition();
	    	reportDefinition.addDataSetDefinition("patientDataSet", dataSetDefinition, null);
	    	ReportData reportData = Context.getService(ReportService.class).evaluate(reportDefinition, evalContext);
			model.addAttribute("reportData", reportData);    	
	    	*/
    		
	    	// Add generated report, patients, and cohort to request
	    	model.addAttribute("patients", Context.getPatientSetService().getPatients(selectedCohort.getMemberIds()));
	    	model.addAttribute("cohort", selectedCohort);    		
    	}    	
    	
    	
    	manageDashboard(model);
    	
    	return "/module/reporting/dashboard/cohortDashboard";
    	
    }
    
	
    /**
     * Manage reporting dashboard.
     * 
     * @param model
     * @return
     */
    //@RequestMapping("/module/reporting/dashboard/manageDashboard")
    public String manageDashboard(ModelMap model) {
    	    	
    	// Get all reporting objects
    	model.addAttribute("cohortDefinitions", 
    			Context.getService(CohortDefinitionService.class).getAllCohortDefinitions(false));
    	model.addAttribute("datasetDefinitions", 
    			Context.getService(DataSetDefinitionService.class).getAllDataSetDefinitions(false));
    	model.addAttribute("indicators", 
    			Context.getService(IndicatorService.class).getAllIndicators(false));
    	model.addAttribute("reportDefinitions", 
    			Context.getService(ReportService.class).getReportDefinitions());
    	model.addAttribute("reportRenderers", 
    			Context.getService(ReportService.class).getReportRenderers());
    	
    	// Get all static data
    	List<Program> programs = Context.getProgramWorkflowService().getAllPrograms();    	
    	model.addAttribute("programs", programs);
    	model.addAttribute("encounterTypes", Context.getEncounterService().getAllEncounterTypes());
    	model.addAttribute("identifierTypes", Context.getPatientService().getAllPatientIdentifierTypes());
    	model.addAttribute("attributeTypes", Context.getPersonService().getAllPersonAttributeTypes());
    	model.addAttribute("drugs", Context.getConceptService().getAllDrugs());
    	//model.addAttribute("concepts", Context.getConceptService().getAllConcepts());
    	//model.addAttribute("tokens", Context.getLogicService().getTokens());
    	//model.addAttribute("tags", Context.getLogicService().findTags(""));
    	model.addAttribute("locations", Context.getLocationService().getAllLocations());
    	//model.addAttribute("locationTags", Context.getLocationService().getAllLocationTags());
    	model.addAttribute("forms", Context.getFormService().getAllForms());    	
    	model.addAttribute("relationshipTypes", Context.getPersonService().getAllRelationshipTypes());
    	//model.addAttribute("relationshipTypes", Context.getPatientSetService().getAllPatients());

    	EvaluationContext evaluationContext = new EvaluationContext();
    	
    	// These should be defined explicitly and configured via global properties
		model.addAttribute("males", getGenderCohort(evaluationContext, "M"));
		model.addAttribute("females", getGenderCohort(evaluationContext, "F"));    	
		model.addAttribute("adults", getAgeCohort(evaluationContext, 15, 150, new Date()));
		model.addAttribute("children", getAgeCohort(evaluationContext, 0, 14, new Date()));				
		model.addAttribute("all", Context.getPatientSetService().getAllPatients());

		Map<Program, Cohort> programCohortMap = new HashMap<Program, Cohort>();
		for (Program program : programs) {
			Cohort cohort = getProgramStateCohort(evaluationContext, program);
			log.info("Program: " + program.getName() + " " + cohort.getSize());
			programCohortMap.put(program, cohort);
		}
		model.addAttribute("programCohortMap", programCohortMap);
		
		return "/module/reporting/dashboard/dashboardManager";
    }    
    
    /**
     * Get program cohort.
     * 
     * @param evaluationContext
     * @param program
     * @return
     */
    public Cohort getProgramStateCohort(EvaluationContext evaluationContext, Program program) { 
    	ProgramStateCohortDefinition programStateCohortDefinition = new ProgramStateCohortDefinition();    	
    	programStateCohortDefinition.setProgram(program);
    	programStateCohortDefinition.setStateList(null);
    	return Context.getService(CohortDefinitionService.class).evaluate(programStateCohortDefinition, evaluationContext);     	
    }
        
    /**
     * Get program cohort.
     * 
     * @param evaluationContext
     * @param program
     * @return
     */
    public Cohort getGenderCohort(EvaluationContext evaluationContext, String gender) {     	
		GenderCohortDefinition genderCohortDefinition = new GenderCohortDefinition();
		genderCohortDefinition.setGender(gender);
		return Context.getService(CohortDefinitionService.class).evaluate(genderCohortDefinition, evaluationContext); 
    }

    /**
     * Get an adult cohort 
     * 
     * @param evaluationContext
     * @param minAge
     * @param maxAge
     * @param effectiveDate
     * @return
     */
    public Cohort getAgeCohort(EvaluationContext evaluationContext, Integer minAge, Integer maxAge, Date effectiveDate) {     	
    	AgeCohortDefinition ageCohortDefinition = new AgeCohortDefinition();
    	ageCohortDefinition.setMinAge(minAge);
    	ageCohortDefinition.setMaxAge(maxAge);
    	ageCohortDefinition.setEffectiveDate(effectiveDate);		
    	return Context.getService(CohortDefinitionService.class).evaluate(ageCohortDefinition, evaluationContext);    
    }
}
