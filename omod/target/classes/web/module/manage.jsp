<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/template/header.jsp"%>

<%@ include file="template/localHeader.jsp"%>

<body>	
       <form id="theForm" action="listVlData.jsp">

           <p>
               <div class="row">
                   <label for="vlState">Estado da carga viral:</label>
                   <select id="selValue" name="vlState"> 
                		<option value="PROCESSED">PROCESSED</option>
                		<option value="NOT_PROCESSED">NOT_PROCESSED</option>
             		</select>
               </div><br>
               <div class="row">
                   <input id="subValue" type="submit" value="Submeter">
               </div>
           </p>

       </form>
</body>

<%@ include file="/WEB-INF/template/footer.jsp"%>