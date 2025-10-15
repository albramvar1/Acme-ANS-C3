<%@page%>
<%@taglib prefix="jstl" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="acme" uri="http://acme-framework.org/"%>

<acme:form readonly="false">
    
    <acme:input-textbox 
                code="crewMember.assignment.form.label.employeeCode" 
                path="flightCrewMember.employeeCode" 
                readonly="true"
            />

    <jstl:choose>
        <jstl:when test="${_command == 'create'}">
        
        	<acme:input-select 
			code="crewMember.assignment.form.label.flightNumber" 
			path="leg"
			choices="${legs}"
			/>
            
            <acme:input-textbox
                code="crewMember.assignment.form.label.assignmentStatus"
                path="assignmentStatus"
                readonly="true"
            />
        </jstl:when>
        <jstl:otherwise>
        	<acme:input-textbox 
			code="crewMember.assignment.form.label.flightNumber" 
			path="leg.flightCode" 
			readonly="true"
			/>
            <acme:input-select 
                code="crewMember.assignment.form.label.assignmentStatus"
                path="assignmentStatus"
                choices="${assignmentStatuses}"
            />
        </jstl:otherwise>
    </jstl:choose>

    <acme:input-select
        code="crewMember.assignment.form.label.crewRole"
        path="crewRole"
        choices="${crewRoles}" 
    />

    <acme:input-moment 
        code="crewMember.assignment.form.label.lastUpdated"
        path="lastUpdated"
        readonly="true"
    />

    <acme:input-textarea
        code="crewMember.assignment.form.label.comments"
        path="comments"
    />

    <jstl:choose>    
        <jstl:when test="${acme:anyOf(_command, 'show|update|publish|delete')}">
            <jstl:if test="${hasLog}">
				<acme:button 
					code="crewMember.assignment.form.button.logs" 
					action="/flight-crew/activity-log/list?masterId=${masterId}"
				/>
			</jstl:if>
            <jstl:if test="${draftMode == true}">
                <acme:submit code="crewMember.assignment.form.button.update" action="/flight-crew/flight-assignment/update"/>
                <acme:submit code="crewMember.assignment.form.button.publish" action="/flight-crew/flight-assignment/publish"/>
                <acme:submit code="crewMember.assignment.form.button.delete" action="/flight-crew/flight-assignment/delete"/>
            </jstl:if>    
        </jstl:when>    
        <jstl:when test="${_command == 'create'}">
            <acme:submit code="crewMember.assignment.form.submit.create" action="/flight-crew/flight-assignment/create"/>
        </jstl:when>
    </jstl:choose>    

</acme:form>