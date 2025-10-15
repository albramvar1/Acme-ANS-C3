
package acme.features.flight_crew.flight_assignment;

import org.springframework.beans.factory.annotation.Autowired;

import acme.client.components.models.Dataset;
import acme.client.components.views.SelectChoices;
import acme.client.helpers.MomentHelper;
import acme.client.services.AbstractGuiService;
import acme.client.services.GuiService;
import acme.datatypes.AssignmentStatus;
import acme.datatypes.CrewDuty;
import acme.entities.flight_assignment.FlightAssignment;
import acme.entities.leg.Leg;
import acme.realms.FlightCrew;

@GuiService
public class CrewFlightAssignmentUpdateService extends AbstractGuiService<FlightCrew, FlightAssignment> {

	// Internal state ---------------------------------------------------------

	@Autowired
	private FlightAssignmentRepository repository;

	// AbstractGuiService interface -------------------------------------------


	@Override
	public void authorise() {
		int id = super.getRequest().getData("id", int.class);
		FlightAssignment assignment = this.repository.findAssignmentById(id);

		boolean isOwner = assignment != null && super.getRequest().getPrincipal().hasRealm(assignment.getFlightCrewMember());
		boolean isDraft = assignment != null && assignment.isDraftMode();

		super.getResponse().setAuthorised(isOwner && isDraft);
	}

	@Override
	public void load() {

		int id;
		FlightAssignment assignment;

		id = super.getRequest().getData("id", int.class);
		assignment = this.repository.findAssignmentById(id);

		super.getBuffer().addData(assignment);

	}

	@Override
	public void bind(final FlightAssignment assignment) {
		super.bindObject(assignment, "crewRole", "assignmentStatus", "comments");

	}

	@Override
	public void validate(final FlightAssignment assignment) {
		Leg leg = assignment.getLeg();
		FlightCrew member = assignment.getFlightCrewMember();
		CrewDuty role = assignment.getCrewRole();

		super.state(assignment.isDraftMode(), "*", "crewMember.assignment.error.not-editable");
		super.state(leg != null, "leg", "crewMember.assignment.error.missing-leg");
		super.state(member != null, "*", "crewMember.assignment.error.missing-member");
		//super.state(role != null, "crewRole", "crewMember.assignment.error.missing-role");

		if (leg != null && member != null)
			if (role == CrewDuty.PILOT || role == CrewDuty.CO_PILOT || role == CrewDuty.LEAD_ATTENDANT) {
				boolean roleAlreadyAssigned = this.repository.existsPublishedAssignmentForLegWithRole(leg.getId(), role);
				boolean isSame = roleAlreadyAssigned && this.repository.findAssignmentById(assignment.getId()).getCrewRole() == role;
				super.state(!roleAlreadyAssigned || isSame, "crewRole", "crewMember.assignment.error.duplicate-role");
			}

	}

	@Override
	public void perform(final FlightAssignment assignment) {
		assignment.setLastUpdated(MomentHelper.getCurrentMoment());
		this.repository.save(assignment);
	}

	@Override
	public void unbind(final FlightAssignment assignment) {

		SelectChoices choicesCrewRol;
		SelectChoices choicesAssignmentStatus;
		Dataset dataset;

		dataset = super.unbindObject(assignment, "crewRole", "lastUpdated", "assignmentStatus", "comments", "leg.flightCode", "flightCrewMember.employeeCode");

		choicesCrewRol = SelectChoices.from(CrewDuty.class, assignment.getCrewRole());
		choicesAssignmentStatus = SelectChoices.from(AssignmentStatus.class, assignment.getAssignmentStatus());

		dataset.put("crewRoles", choicesCrewRol);
		dataset.put("assignmentStatuses", choicesAssignmentStatus);
		dataset.put("masterId", assignment.getId());

		dataset.put("draftMode", assignment.isDraftMode());

		super.getResponse().addData(dataset);
	}
}
