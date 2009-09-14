<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="../run/localHeader.jsp"%>

<script type="text/javascript" charset="utf-8">
$(document).ready(function() {
	$('#dataset-preview-table').dataTable( {
		"bPaginate": true,
		//"sPaginationType": "full_numbers",
		"bLengthChange": false,
		"bFilter": true,
		"bSort": true,
		"bInfo": true,
		"bAutoWidth": true
		//"sDom": '<"top"i>rt<"bottom"flp<"clear">'
	} );	

	$("#show-columns").click(function(event){ 
		// eventually will show/hide appropriate columns based on selected dataset definition
	});


} );
</script>

<style>
	input,select { font-size: medium; } 
	legend { font-size: large; } 
	label { font-size: medium; font-weight: bold; } 
	.desc { display: block; }
</style>


<div id="page">
	<div id="container">
	
		<div id="datasetPreview" style="margin-top: 10px;">
				
				<form id="datasetForm" name="datasetForm" class="wufoo topLabel" autocomplete="off"
					method="get" action="${pageContext.request.contextPath}/module/reporting/datasets/viewDataSet.form">
	
	
					<!--  
					<input type="hidden" id="id" name="id" value="${dataSetDefinition.id}"/>
					<input type="hidden" id="uuid" name="uuid" value="${dataSetDefinition.uuid}"/>
					
					-->
					
					<input type="hidden" id="type" name="type" value="${dataSetDefinition.class.name}"/>
					
					<fieldset style="padding: 25px; width: 100%;">
						<legend>Step 1.  Configure your dataset</legend>
						<span>
							<select id="dataset-select" name="dataSetId">	
								<option value="">Choose a dataset definition ...</option>					
								<c:forEach var="dsdOption" items="${dataSetDefinitions}">
									<c:if test="${empty dsdOption.parameters && !empty dsdOption.uuid}">
										<c:set var="isSelected"></c:set>
										<c:choose>
											<c:when test="${dataSetDefinition.uuid == dsdOption.uuid}">
												<c:set var="isSelected">selected="selected"</c:set>
											</c:when>					
										</c:choose>
										<option ${isSelected} value="${dsdOption.uuid}">
											${dsdOption.name}
										</option>
									</c:if>
								</c:forEach>								
							</select><!-- <a href="#" id="show-columns">Show Columns</a> -->
						</span>
						<span>				
							<select id="cohort-select" name="cohortId">
								<option value="all">Choose a filter ...</option>						
								<option value="all">All patients [default]</option>						
								<c:forEach var="cdOption" items="${cohortDefinitions}" >
									<c:if test="${empty cdOption.parameters && !empty cdOption.uuid}">
										<c:set var="isSelected"></c:set>
										<c:choose>
											<c:when test="${cohortDefinition.uuid == cdOption.uuid || cohortDefinition.id == cdOption.id}">
												<c:set var="isSelected">selected="selected"</c:set>
											</c:when>					
										</c:choose>
										<option ${isSelected} value="${cdOption.uuid}">${cdOption.name}</i></option>
									</c:if>
								</c:forEach>								
							</select>						
						</span>

						<!--  Eventually this will be used to show columns for selected dataset -->
						<span>
							<c:forEach var="dsd" items="${dataSetDefinitions}">											
								<c:if test="${!empty dsd.columns}">					
									<div id="dataset-columns-${dsd.uuid}" style="display:none;">
										<ul>
											<c:forEach var="column" items="${dsd.columns}" varStatus="varStatus">
												<li>${column.displayName}</li>
											</c:forEach>
										</ul>
									</div>
								</c:if>
							</c:forEach>						
						</span>

						<span>					
							<select id="limit" name="limit">
								<option <c:if test="${param.limit=='0'}">selected</c:if> value="all">Show me all records (this may take awhile)</option>
								<option <c:if test="${param.limit=='10'}">selected</c:if> value="10">Only show the first 10 records</option>
								<option <c:if test="${param.limit=='100'}">selected</c:if> value="100">Only show the first 100 records</option>
								<option <c:if test="${param.limit=='500'}">selected</c:if> value="500">Only show the first 500 records</option>
								<option <c:if test="${param.limit=='1000'}">selected</c:if> value="1000">Only show the first 1000 records</option>
							</select>		
						</span>
						

						<span align="left">
							<input type="submit" value="Go"/> get me some data!					
						</span>
						
					</fieldset>
				</form>	
				
			<c:if test="${!empty dataSet}">
				<fieldset style="padding: 25px; margin-bottom: 50px; width: 100%;">
					<legend>Step 2.  Preview your dataset</legend>

					
					<div align="right">
						<strong>Download:</strong> 
						<a href="${pageContext.request.contextPath}/module/reporting/datasets/downloadDataSet.form?limit=${param.limit}&format=csv&dataSetId=${dataSetDefinition.uuid}&cohortId=${cohortDefinition.uuid}&type=${dataSetDefinition.class.name}">csv</a> |
						<a href="${pageContext.request.contextPath}/module/reporting/datasets/downloadDataSet.form?limit=${param.limit}&format=tsv&dataSetId=${dataSetDefinition.uuid}&cohortId=${cohortDefinition.uuid}&type=${dataSetDefinition.class.name}">tsv</a> |
						<a href="${pageContext.request.contextPath}/module/reporting/datasets/downloadDataSet.form?limit=${param.limit}&format=xml&dataSetId=${dataSetDefinition.uuid}&cohortId=${cohortDefinition.uuid}&type=${dataSetDefinition.class.name}">xml</a> |
						<a href="${pageContext.request.contextPath}/module/reporting/datasets/downloadDataSet.form?limit=${param.limit}&format=web&dataSetId=${dataSetDefinition.uuid}&cohortId=${cohortDefinition.uuid}&type=${dataSetDefinition.class.name}">web</a> |
						<a href="${pageContext.request.contextPath}/module/reporting/datasets/downloadDataSet.form?limit=${param.limit}&format=xls&dataSetId=${dataSetDefinition.uuid}&cohortId=${cohortDefinition.uuid}&type=${dataSetDefinition.class.name}">xls</a>
						
					</div>
					<div>						


						<div style="margin-bottom: 25px; font-size: medium; width:75%;">
							<c:if test="${dataSetDefinition != null}">
								<c:set var="recordCount">
									<c:choose>
										<c:when test="${param.limit!='all'}">${param.limit}</c:when>
										<c:otherwise>${cohort.size}</c:otherwise>
									</c:choose>
								</c:set>
								Returned <strong>${param.limit} records </strong>
								from the <strong>${dataSetDefinition.name}</strong>								
									<c:choose>
										<c:when test="${!empty cohortDefinition}">
											filtered by  the <strong>${cohortDefinition.name}</strong> cohort definition.</c:when>
										<c:otherwise>
											showing <strong>all active patients</strong> in the database.
										</c:otherwise>
									</c:choose>
							</c:if>
						</div>

	
						<table id="dataset-preview-table" class="display">
							<thead>
								<tr>
									<c:forEach var="column" items="${dataSetDefinition.columns}" varStatus="varStatus">				
										<th>
											${column.displayName}
										</th>
									</c:forEach>
								</tr>
							</thead>
							<tbody>						
									<c:forEach var="dataSetRow" items="${dataSet.rows}" varStatus="varStatus">
										<tr>
										
											<c:forEach var="column" items="${dataSetDefinition.columns}" varStatus="varStatus">				
												<td>
													${dataSetRow.columnValues[column]}
												</td>
											</c:forEach>										
										</tr>
									</c:forEach>
							</tbody>
							<tfoot>
							</tfoot>
						</table>
					</div>					
				</fieldset>
												
			</c:if>											
				
				
			</div>
		</div>			
	</div>
</div>


<%@ include file="/WEB-INF/template/footer.jsp"%>