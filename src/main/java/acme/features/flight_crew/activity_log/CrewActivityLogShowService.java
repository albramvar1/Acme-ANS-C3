
package acme.features.flight_crew.activity_log;

import org.springframework.beans.factory.annotation.Autowired;

import acme.client.components.models.Dataset;
import acme.client.helpers.MomentHelper;
import acme.client.services.AbstractGuiService;
import acme.client.services.GuiService;
import acme.datatypes.AssignmentStatus;
import acme.entities.activity_log.ActivityLog;
import acme.realms.FlightCrew;

@GuiService
public class CrewActivityLogShowService extends AbstractGuiService<FlightCrew, ActivityLog> {

	// Internal state ---------------------------------------------------------

	@Autowired
	private ActivityLogRepository repository;

	// AbstractGuiService interface -------------------------------------------


	@Override
	public void authorise() {
		boolean authorised = false;
		int logId;
		ActivityLog log;

		if (super.getRequest().hasData("id", int.class)) {
			logId = super.getRequest().getData("id", int.class);
			log = this.repository.findOneById(logId);

			if (log != null) {
				boolean legFinished = !log.getFlightAssignment().getLeg().getScheduledArrival().after(MomentHelper.getCurrentMoment());
				authorised = super.getRequest().getPrincipal().hasRealm(log.getFlightAssignment().getFlightCrewMember()) && log.getFlightAssignment().getAssignmentStatus() == AssignmentStatus.CONFIRMED && !log.getFlightAssignment().isDraftMode()
					&& log.getFlightAssignment().getLeg().isPublished() && legFinished;
			}
		}

		super.getResponse().setAuthorised(authorised);
	}

	@Override
	public void load() {

		ActivityLog log;
		int logId;

		logId = super.getRequest().getData("id", int.class);
		log = this.repository.findOneById(logId);

		super.getBuffer().addData(log);

	}

	@Override
	public void unbind(final ActivityLog log) {
		Dataset dataset;

		dataset = super.unbindObject(log, "registrationMoment", "typeOfIncident", "description", "severityLevel");
		dataset.put("validDraft", log.isDraftMode() && !log.getFlightAssignment().isDraftMode() && log.getFlightAssignment().getLeg().isPublished());
		dataset.put("masterId", log.getFlightAssignment().getId());
		super.getResponse().addData(dataset);
	}

}
