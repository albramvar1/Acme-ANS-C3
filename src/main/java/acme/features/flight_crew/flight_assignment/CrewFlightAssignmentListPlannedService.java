
package acme.features.flight_crew.flight_assignment;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;

import acme.client.components.models.Dataset;
import acme.client.helpers.MomentHelper;
import acme.client.services.AbstractGuiService;
import acme.client.services.GuiService;
import acme.entities.flight_assignment.FlightAssignment;
import acme.realms.FlightCrew;

@GuiService
public class CrewFlightAssignmentListPlannedService extends AbstractGuiService<FlightCrew, FlightAssignment> {

	// Internal state ---------------------------------------------------------

	@Autowired
	private FlightAssignmentRepository repository;

	// AbstractGuiService interface -------------------------------------------


	@Override
	public void authorise() {
		super.getResponse().setAuthorised(true);
	}

	@Override
	public void load() {

		Collection<FlightAssignment> assignments;
		int flightCrewMemberId;

		flightCrewMemberId = super.getRequest().getPrincipal().getActiveRealm().getId();
		assignments = this.repository.findAssignmentsPlannedByMemberId(flightCrewMemberId);

		Collection<FlightAssignment> planned = assignments.stream().filter(a -> a.getLeg().getScheduledArrival().after(MomentHelper.getCurrentMoment())).toList();

		super.getBuffer().addData(planned);

	}

	@Override
	public void unbind(final FlightAssignment assignment) {
		Dataset dataset;

		dataset = super.unbindObject(assignment, "leg.flightCode", "crewRole", "leg.departureAirport.name", "leg.arrivalAirport.name");

		super.getResponse().addData(dataset);

	}
}
