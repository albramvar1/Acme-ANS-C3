<%@page%>

<%@taglib prefix="jstl" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="acme" uri="http://acme-framework.org/"%>

<acme:list>
	<acme:list-column code="crewMember.assignment.list.completed.label.flightNumber" path="leg.flightCode"/>
	<acme:list-column code="crewMember.assignment.list.completed.label.crewRole" path="crewRole"/>
	<acme:list-column code="crewMember.assignment.list.completed.label.departureAirport.name" path="leg.departureAirport.name"/>
	<acme:list-column code="crewMember.assignment.list.completed.label.arrivalAirport.name" path="leg.arrivalAirport.name"/>
	<acme:list-payload path="payload"/>
</acme:list>

<jstl:if test="${_command == 'list-planned'}">
    <acme:button 
        code="crewMember.assignment.list.button.create" 
        action="/flight-crew/flight-assignment/create"
    />
</jstl:if>