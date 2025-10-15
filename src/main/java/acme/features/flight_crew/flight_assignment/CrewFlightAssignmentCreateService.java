
package acme.features.flight_crew.flight_assignment;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;

import acme.client.components.models.Dataset;
import acme.client.components.views.SelectChoices;
import acme.client.helpers.MomentHelper;
import acme.client.services.AbstractGuiService;
import acme.client.services.GuiService;
import acme.datatypes.AssignmentStatus;
import acme.datatypes.Availability;
import acme.datatypes.CrewDuty;
import acme.entities.flight_assignment.FlightAssignment;
import acme.entities.leg.Leg;
import acme.realms.FlightCrew;

@GuiService
public class CrewFlightAssignmentCreateService extends AbstractGuiService<FlightCrew, FlightAssignment> {

	// Internal state ---------------------------------------------------------

	@Autowired
	private FlightAssignmentRepository repository;

	// AbstractGuiService interface -------------------------------------------


	@Override
	public void authorise() {
		boolean isAuthorised = super.getRequest().getPrincipal().hasRealmOfType(FlightCrew.class);

		// Recuperar el legId de forma segura
		Integer legId = super.getRequest().getData("leg", int.class, null);

		if (legId != null && legId != 0) {
			Collection<Leg> availableLegs = this.repository.findAllLegsAvailableForAssignment(MomentHelper.getCurrentMoment());
			boolean legIsAvailable = availableLegs.stream().anyMatch(l -> l.getId() == legId);
			isAuthorised = isAuthorised && legIsAvailable;
		}

		super.getResponse().setAuthorised(isAuthorised);
	}

	@Override
	public void load() {

		int crewMemberId = super.getRequest().getPrincipal().getActiveRealm().getId();
		FlightCrew crewMember = this.repository.findCrewMemberById(crewMemberId);

		FlightAssignment assignment = new FlightAssignment();
		assignment.setDraftMode(true);
		assignment.setAssignmentStatus(AssignmentStatus.PENDING);
		assignment.setLastUpdated(MomentHelper.getCurrentMoment());
		assignment.setFlightCrewMember(crewMember);

		super.getBuffer().addData(assignment);
	}

	@Override
	public void bind(final FlightAssignment assignment) {
		int legId = super.getRequest().getData("leg", int.class);
		Leg leg = this.repository.findLegById(legId);

		super.bindObject(assignment, "crewRole", "comments");
		assignment.setLeg(leg);
	}

	@Override
	public void validate(final FlightAssignment assignment) {
		Leg leg = assignment.getLeg();
		FlightCrew member = assignment.getFlightCrewMember();
		CrewDuty role = assignment.getCrewRole();

		super.state(leg != null, "leg", "crewMember.assignment.error.missing-leg");
		super.state(member != null, "*", "crewMember.assignment.error.missing-member");
		super.state(role != null, "crewRole", "crewMember.assignment.error.missing-role");

		if (leg != null && member != null) {

			// 1. Evitar duplicados
			boolean duplicateAssignment = this.repository.existsAssignmentForLegAndCrewMember(leg.getId(), member.getId());
			super.state(!duplicateAssignment, "leg", "crewMember.assignment.error.duplicate-assignment");

			// 2. Solo un piloto y copiloto por etapa
			if (role == CrewDuty.PILOT || role == CrewDuty.CO_PILOT || role == CrewDuty.LEAD_ATTENDANT) {
				boolean roleAlreadyAssigned = this.repository.existsPublishedAssignmentForLegWithRole(leg.getId(), role);
				super.state(!roleAlreadyAssigned, "crewRole", "crewMember.assignment.error.duplicate-role");
			}

			// 3. El miembro debe estar disponible
			boolean isAvailable = member.getAvailability() == Availability.AVAILABLE;
			super.state(isAvailable, "*", "crewMember.assignment.error.not-available");

			// 4. La etapa debe estar publicada y no haber ocurrido aún
			boolean legNotOccurred = leg.isPublished() && leg.getScheduledArrival().after(MomentHelper.getCurrentMoment());
			super.state(legNotOccurred, "leg", "crewMember.assignment.error.leg-occurred");

			// 5. No debe haber solapamiento
			boolean isOverlapping = this.repository.existsOverlappingAssignment(member.getId(), leg.getScheduledDeparture(), leg.getScheduledArrival());
			super.state(!isOverlapping, "*", "crewMember.assignment.error.overlapping");
		}
	}

	@Override
	public void perform(final FlightAssignment assignment) {
		this.repository.save(assignment);
	}

	@Override
	public void unbind(final FlightAssignment assignment) {
		Collection<Leg> availableLegs = this.repository.findAllLegsAvailableForAssignment(MomentHelper.getCurrentMoment());
		SelectChoices choicesLegs = SelectChoices.from(availableLegs, "flightCode", assignment.getLeg());

		SelectChoices choicesCrewRol = SelectChoices.from(CrewDuty.class, assignment.getCrewRole());

		Dataset dataset = super.unbindObject(assignment, "crewRole", "lastUpdated", "assignmentStatus", "comments", "flightCrewMember.employeeCode");

		dataset.put("legs", choicesLegs);
		dataset.put("leg", choicesLegs.getSelected().getKey());
		dataset.put("crewRoles", choicesCrewRol);

		super.getResponse().addData(dataset);
	}
}
