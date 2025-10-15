<%@page%>

<%@taglib prefix="jstl" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="acme" uri="http://acme-framework.org/"%>

<acme:list>
	<acme:list-column code="crewMember.log.list.label.flightNumber" path="flightAssignment.leg.flightCode"/>
	<acme:list-column code="crewMember.log.list.label.crewRole" path="flightAssignment.crewRole"/>
	<acme:list-column code="crewMember.log.list.label.typeOfIncident" path="typeOfIncident"/>
	<acme:list-column code="crewMember.log.list.label.severityLevel" path="severityLevel"/>
	<acme:list-payload path="payload"/>
</acme:list>

<jstl:if test="${canCreate}">
    <acme:button code="crewMember.log.list.button.create" action="/flight-crew/activity-log/create?masterId=${masterId}"/>
</jstl:if>