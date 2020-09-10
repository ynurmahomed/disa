<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/template/header.jsp"%>

<%@ include file="template/localHeader.jsp"%>


<%@ page import="java.util.*,
			org.openmrs.api.context.Context,
			org.openmrs.Patient"%> 	
			
<form method="post">

<h2>Mapping Patient Between Disa and OpenMrs</h2>

	<div class="row">
		<table>
		  <tr colspan="8" align="left">
		  	<th>Disa Data</th>
		  </tr>
		  <tr>
		    <td>
		    	<table border="1">
				  <tr>
					<td>NID</td>
					<td>Nome</td>
					<td>Sexo</td>
					<td>Idade</td>
					<td>ID(Requisição)</td>
					<td>Carga Viral(Cópia)</td>
					<td>Carga Viral(Log)</td>
					<td>Carga Viral(Coded)</td>  
				  </tr>
				  <tr>
				    <td><%= request.getParameter("nid") %></td>
				    <td><%= request.getParameter("name") %></td>
				    <td><%= request.getParameter("gender") %></td>
				    <td><%= request.getParameter("age") %></td>
				    <td><%= request.getParameter("requestId") %></td>
				    <td><%= request.getParameter("vlCopies") %></td>
				    <td><%= request.getParameter("vlLogs") %></td>
				    <td><%= request.getParameter("vlCoded") %></td>
				  </tr>
				</table>
		    </td>
		  </tr>		  
		</table>
	</div><br><br>
	
	<div class="row">
		<table>
		  <tr colspan="8" align="left">
		    <th>OpenMrs Data</th>
		  </tr>
		  <tr>
		    <td>
		    	<table border="1">
				  <tr>
					<td>NID</td>
					<td colspan = "4">Nome</td>
					<td>Sexo</td>
					<td>Idade</td>
					<td>Data de Nascimento</td>
					<td>Selecionar</td>
				  </tr>
				  
				  <% 
				  		List<Patient> listaPaciente = Context.getPatientService().getPatientsByName(request.getParameter("name"));
				  			for (Patient paciente : listaPaciente ) {
				  %>				
				  <tr>
				    <td><%= paciente.getIdentifiers() %></td>
				    <td colspan = "4"><%= paciente.getGivenName() + " " + paciente.getMiddleName() + " " + paciente.getFamilyName()%></td>
				    <td><%= paciente.getGender() %></td>
				    <td><%= paciente.getAge() %></td>
				    <td><%= paciente.getBirthdate() %></td>
				    <td>
				    	<label  class="radio-inline">
	       					<input type="radio" name="idPatient" id="idPatient" checked="checked" value = "<%= paciente.getId() %>"/>Selecionar
	     				</label>
				    </td>
				  </tr>
				  <%
				  	}
				  %>
				</table>
		    </td>
		  </tr>
		</table>
	</div><br><br>
	
	<div class="row">
		<input id="subValue" type="submit" value="Mapeiar"><br><br>
	</div>	
</form>
<%@ include file="/WEB-INF/template/footer.jsp"%>