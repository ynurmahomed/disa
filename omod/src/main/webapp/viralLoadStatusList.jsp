<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/template/header.jsp"%>

<%@ include file="template/localHeader.jsp"%>

<style>
	#vlResultsTable {
		font-family: "Trebuchet MS", Arial, Helvetica, sans-serif;
		border-collapse: collapse;
		width: 100%;
	}
	
	#vlResultsTable td, #vlResultsTable th {
		border: 1px solid #ddd;
		padding: 8px;
	}
	
	#vlResultsTable tr:nth-child(even) {
		background-color: #f2f2f2;
	}
	
	#vlResultsTable tr:hover {
		background-color: #ddd;
	}
	
	#vlResultsTable th {
		padding-top: 12px;
		padding-bottom: 12px;
		text-align: left;
		background-color: #1aac9b;
		color: white;
	}
	
	.submit-btn {
		flex: 1;
		margin: 10px 15px;
	}
	
	.submit-btn input {
		color: #fff;
		background: #1aac9b;
		padding: 8px;
		width: 12.8em;
		font-weight: bold;
		text-shadow: 0 0 .3em black;
		font-size: 9pt;
		border-radius: 5px 5px;
	}
</style>

<h2><openmrs:message code="disa.list.viral.load.results"/></h2>
<br />

<b class="boxHeader"><spring:message code="disa.select.viral.load.status" /></b>
<fieldset>
	<form method="post">
	
	    <p>
	        <div class="row">
	            <label for="vlState"><openmrs:message code="disa.viral.load.status"/>:</label>
	            <select id="selValue" name="vlState"> 
	         		<option value="PROCESSED"><openmrs:message code="disa.viral.load.status.processed"/></option>
	         		<option value="NOT_PROCESSED"><openmrs:message code="disa.viral.load.status.not_processed"/></option>
	      		</select>
	        </div><br>
	        <div class="submit-btn">
	            <input id="subValue" type="submit" value='<openmrs:message code="general.next"/>'>
	        </div>
	    </p>
	
	</form>
</fieldset>
<%@ include file="/WEB-INF/template/footer.jsp"%>