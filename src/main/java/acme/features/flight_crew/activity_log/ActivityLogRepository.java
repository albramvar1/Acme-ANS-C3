
package acme.features.flight_crew.activity_log;

import java.util.Collection;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import acme.client.repositories.AbstractRepository;
import acme.entities.activity_log.ActivityLog;
import acme.entities.flight_assignment.FlightAssignment;

@Repository
public interface ActivityLogRepository extends AbstractRepository {

	@Query("SELECT al FROM ActivityLog al WHERE al.id = :id")
	ActivityLog findOneById(int id);

	@Query("""
		    SELECT log
		    FROM ActivityLog log
		    WHERE log.flightAssignment.id = :masterId
		""")
	Collection<ActivityLog> findLogsByMasterId(int masterId);

	@Query("SELECT a FROM FlightAssignment a WHERE a.id = :id")
	FlightAssignment findAssignmentById(int id);

}
