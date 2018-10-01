package org.springframework.samples.travel;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

/**
 * A JPA-based implementation of the Booking Service. Delegates to a JPA entity
 * manager to issue data access calls against the backing repository. The
 * EntityManager reference is provided by the managing container (Spring)
 * automatically.
 */
@Service("bookingService")
@Repository
public class JpaBookingService implements BookingService {

	private EntityManager em;
	private Log log = LogFactory.getFactory().getInstance(JpaBookingService.class);

	@PersistenceContext
	public void setEntityManager(EntityManager em) {
		this.em = em;
	}

	@Transactional(readOnly = true)
	@SuppressWarnings("unchecked")
	public List<Booking> findBookings(String username) {
		if (username != null) {
			List<Booking> bookList = null;
			log.info("from log4j=== findBookings log4j info log");
			log.debug("from log4j=== findBookings log4j debug log");
			log.error("from log4j=== findBookings log4j error log");
			bookList = em
					.createQuery("select b from Booking b where b.user.username = :username order by b.checkinDate")
					.setParameter("username", username).getResultList();
			if (!CollectionUtils.isEmpty(bookList)) {
				for (Booking booking : bookList) {
					List<Review> reviewList = findReview(booking.getHotel().getId());
					if (!CollectionUtils.isEmpty(reviewList)) {
						for (Review r : reviewList) {
							if (username.equals(r.getUsername())) {
								booking.setReview(r);
							}
						}
					}
				}
			}
			return bookList;
		} else {
			return null;
		}
	}

	@Transactional(readOnly = true)
	@SuppressWarnings("unchecked")
	public List<Hotel> findHotels(SearchCriteria criteria) {
		String pattern = getSearchPattern(criteria);
		log.info("from log4j=== findHotels log4j info log");
		log.debug("from log4j=== findHotels log4j debug log");
		log.error("from log4j=== findHotels log4j error log");
		return em
				.createQuery("select h from Hotel h where lower(h.name) like " + pattern + " or lower(h.city) like "
						+ pattern + " or lower(h.zip) like " + pattern + " or lower(h.address) like " + pattern)
				.setMaxResults(criteria.getPageSize()).setFirstResult(criteria.getPage() * criteria.getPageSize())
				.getResultList();
	}

	private List<Review> findReview(Long hotelId) {
		List<Review> reviewList = em.createQuery("Select r from Review r where r.hotelid =" + hotelId).getResultList();

		return reviewList;
	}

	@Transactional(readOnly = true)
	public Hotel findHotelById(Long id) {

		Hotel h = em.find(Hotel.class, id);
		if (h != null) {
			String reviews = "";
			List<Review> reviewList = findReview(h.getId());
			if (!CollectionUtils.isEmpty(reviewList)) {
				for (Review r : reviewList) {
//				Review r =  em.find(Review.class, h.getId());
					if (r != null && h.getId().compareTo(r.getHotelid()) == 0)
						reviews = reviews + "  <br/>  " + "User :" + r.getUsername() + " reviewed :" + r.getReview();
				}
			}
			h.setReview(reviews);
		}
		return h;
	}

	@Transactional(readOnly = true)
	public Booking createBooking(Long hotelId, String username) {
		Hotel hotel = em.find(Hotel.class, hotelId);
		User user = findUser(username);
		Booking booking = new Booking(hotel, user);
		em.persist(booking);
		return booking;
	}

	@Transactional
	public void cancelBooking(Long id) {
		Booking booking = em.find(Booking.class, id);
		if (booking != null) {
			em.remove(booking);
		}
	}

	// helpers

	private String getSearchPattern(SearchCriteria criteria) {
		if (StringUtils.hasText(criteria.getSearchString())) {
			return "'%" + criteria.getSearchString().toLowerCase().replace('*', '%') + "%'";
		} else {
			return "'%'";
		}
	}

	private User findUser(String username) {
		return (User) em.createQuery("select u from User u where u.username = :username")
				.setParameter("username", username).getSingleResult();
	}

	@Override
	@Transactional
	public void postReview(Review r) {
		Query query = em.createNativeQuery(
				"insert into Review ( hotelid, username, review) VALUES (:hotelid, :username, :review)");
		query.setParameter("hotelid", r.getHotelid());
		query.setParameter("username", r.getUsername());
		query.setParameter("review", r.getReview());
		query.executeUpdate();

	}

}