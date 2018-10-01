package org.springframework.samples.travel;

import java.security.Principal;
import java.util.List;

import javax.inject.Inject;
import javax.validation.Valid;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class HotelsController {

	private BookingService bookingService;

	@Inject
	public HotelsController(BookingService bookingService) {
		this.bookingService = bookingService;
	}

	@RequestMapping(value = "/hotels/search", method = RequestMethod.GET)
	public void search(SearchCriteria searchCriteria, Principal currentUser, Model model) {
		if (currentUser != null) {

			List<Booking> booking = bookingService.findBookings(currentUser.getName());
			model.addAttribute(booking);
			Review r = new Review();
			if (!CollectionUtils.isEmpty(booking)) {
				for (Booking b : booking) {
					if (b.getReview() != null)
						r = b.getReview();
				}
			}
			model.addAttribute(r);
		}
	}

	@RequestMapping(value = "/hotels", method = RequestMethod.GET)
	public String list(SearchCriteria criteria, Model model) {
		List<Hotel> hotels = bookingService.findHotels(criteria);
		model.addAttribute(hotels);
		return "hotels/list";
	}

	@RequestMapping(value = "/hotels/{id}", method = RequestMethod.GET)
	public String show(@PathVariable Long id, Model model) {
		model.addAttribute(bookingService.findHotelById(id));
		return "hotels/show";
	}

	@RequestMapping(value = "/bookings/{id}", method = RequestMethod.DELETE)
	public String deleteBooking(@PathVariable Long id) {
		bookingService.cancelBooking(id);
		return "redirect:../hotels/search";
	}

	@RequestMapping(value = "/bookings/review", method = RequestMethod.POST)
	public String reviewHotel(@Valid @ModelAttribute("review") Review review, BindingResult bindingResult) {

		bookingService.postReview(review);

		return "redirect:../hotels/search";
	}

}