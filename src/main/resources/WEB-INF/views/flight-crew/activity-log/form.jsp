<%@page%>

<%@taglib prefix="jstl" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="acme" uri="http://acme-framework.org/"%>

<acme:form>

    <acme:input-moment 
        code="crewMember.log.form.label.registrationMoment" 
        path="registrationMoment"
        readonly="true"
    />
    
    <acme:input-textbox 
        code="crewMember.log.form.label.typeOfIncident" 
        path="typeOfIncident" 
    />
    
    <acme:input-textarea
        code="crewMember.log.form.label.description"
        path="description"
    />
    
    <acme:input-integer code="crewMember.log.form.label.severityLevel" path="severityLevel" placeholder="crewMember.log.form.placeholder.severityLevel"/>
    
    <jstl:choose>
    <jstl:when test="${acme:anyOf(_command, 'show|update|delete|publish')}">
    	<jstl:if test="${validDraft == true}">
    		<acme:submit code="crewMember.log.list.button.update" action="/flight-crew/activity-log/update"/>
        	<acme:submit code="crewMember.log.list.button.delete" action="/flight-crew/activity-log/delete"/>
    		<acme:submit code="crewMember.log.list.button.publish" action="/flight-crew/activity-log/publish"/>
		</jstl:if> 
    </jstl:when>
    <jstl:when test="${_command == 'create'}">
        <acme:submit code="crewMember.log.list.button.create" action="/flight-crew/activity-log/create?masterId=${masterId}"/>
    </jstl:when>
</jstl:choose>
    	
</acme:form>