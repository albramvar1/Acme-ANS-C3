
package acme.features.flight_crew.flight_assignment;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import acme.client.repositories.AbstractRepository;
import acme.datatypes.CrewDuty;
import acme.entities.activity_log.ActivityLog;
import acme.entities.flight_assignment.FlightAssignment;
import acme.entities.leg.Leg;
import acme.realms.FlightCrew;

@Repository
public interface FlightAssignmentRepository extends AbstractRepository {

	@Query("""
			SELECT fa
			FROM FlightAssignment fa
			WHERE fa.flightCrewMember.id = :memberId
			  AND fa.assignmentStatus = 'CONFIRMED'
			  AND fa.draftMode = false
			  AND fa.leg.published = true
			ORDER BY fa.leg.scheduledArrival DESC,
				fa.id                    ASC
		""")
	Collection<FlightAssignment> findAssignmentsCompletedByMemberId(int memberId);

	@Query("""
			SELECT fa
			FROM FlightAssignment fa
			WHERE fa.flightCrewMember.id = :memberId
			  AND fa.leg.published = true
			ORDER BY fa.leg.scheduledArrival ASC,
				fa.id                    ASC
		""")
	Collection<FlightAssignment> findAssignmentsPlannedByMemberId(int memberId);

	@Query("""
		    SELECT fa
		    FROM FlightAssignment fa
		    WHERE fa.id = :assignmentId
		""")
	FlightAssignment findAssignmentById(int assignmentId);

	@Query("""
		    SELECT log
		    FROM ActivityLog log
		    WHERE log.flightAssignment.id = :assignmentId
		""")
	Collection<ActivityLog> findLogsByAssignmentId(int assignmentId);

	@Query("""
		    SELECT f
		    FROM FlightCrew f
		    WHERE f.availability = 'AVAILABLE'
		""")
	Collection<FlightCrew> findAllAvailableCrewMembers();

	@Query("SELECT cm FROM FlightCrew cm WHERE cm.id = :id")
	FlightCrew findCrewMemberById(int id);

	@Query("""
			SELECT COUNT(a) > 0
			FROM FlightAssignment a
			WHERE a.leg.id = :legId
			  AND a.flightCrewMember.id = :memberId
			  AND a.draftMode = false
			  AND a.leg.published = true
		""")
	boolean existsPublishedAssignmentForLegAndCrewMember(int legId, int memberId);

	@Query("""
			SELECT COUNT(a) > 0
			FROM FlightAssignment a
			WHERE a.leg.id = :legId
			  AND a.flightCrewMember.id = :memberId
			  AND a.leg.published = true
		""")
	boolean existsAssignmentForLegAndCrewMember(int legId, int memberId);

	@Query("""
			SELECT COUNT(a) > 0
			FROM FlightAssignment a
			WHERE a.leg.id = :legId
			  AND a.crewRole = :role
			  AND a.draftMode = false
			  AND a.leg.published = true
		""")
	boolean existsPublishedAssignmentForLegWithRole(int legId, CrewDuty role);

	@Query("""
			SELECT COUNT(a) > 0
			FROM FlightAssignment a
			WHERE a.flightCrewMember.id = :memberId
			  AND a.draftMode = false
			  AND a.leg.published = true
			  AND (
			      a.leg.scheduledDeparture <= :end AND a.leg.scheduledArrival >= :start
			  )
		""")
	boolean existsOverlappingAssignment(int memberId, Date start, Date end);

	@Query("SELECT l FROM Leg l WHERE l.id = :id")
	Leg findLegById(int id);

	@Query("""
			SELECT l
			FROM Leg l
			WHERE l.published = true
			  AND l.scheduledArrival > :now
			ORDER BY l.scheduledDeparture ASC,
				l.id                   ASC
		""")
	Collection<Leg> findAllLegsAvailableForAssignment(Date now);

	@Query("""
			SELECT a
			FROM FlightAssignment a
			WHERE a.leg.id = :legId AND a.crewRole = :role
		""")
	FlightAssignment findAssignmentByLegIdAndRole(int legId, CrewDuty role);

	@Query("""
		SELECT a
		FROM FlightAssignment a
		""")
	List<FlightAssignment> findAllAssignments();

}
