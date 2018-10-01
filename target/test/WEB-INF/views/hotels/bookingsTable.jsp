<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="security"
	uri="http://www.springframework.org/security/tags"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<c:url var="reviewUrl" value="/bookings/review"/>
<div id="bookings" "class="section">
	<security:authorize ifAllGranted="ROLE_USER">
		<h2>Current Hotel Bookings</h2>

		<c:if test="${empty bookingList}">
			<tr>
				<td colspan="7">No bookings found</td>
			</tr>
		</c:if>

		<c:if test="${!empty bookingList}">
			<table class="summary">
				<thead>
					<tr>
						<th>Name</th>
						<th>Address</th>
						<th>City, State</th>
						<th>Check in Date</th>
						<th>Check out Date</th>
						<th>Confirmation Number</th>
						<th>Action</th>
						<th>Post Review</th>
					</tr>
				</thead>
				<tbody>
					<c:forEach var="booking" items="${bookingList}">
						<tr>
							<td>${booking.hotel.name}</td>
							<td>${booking.hotel.address}</td>
							<td>${booking.hotel.city},${booking.hotel.state}</td>
							<td>${booking.checkinDate}</td>
							<td>${booking.checkoutDate}</td>
							<td>${booking.id}</td>
							<td><spring:url var="bookingUrl" value="/bookings/{id}">
									<spring:param name="id" value="${booking.id}" />
								</spring:url> <form:form action="${bookingUrl}" method="delete">
									<button type="submit">Cancel</button>
								</form:form></td>
							<td><form action="${reviewUrl}" method="POST"  modelAttribute="review" acceptCharset="UTF-8">
									<form:hidden path="review.username" value="${booking.user.username}" />
									<form:hidden path="review.hotelid" value="${booking.hotel.id}" />
									<form:input path="review.review" />
									<button type="submit">Post</button>
								</form></td>
						</tr>
					</c:forEach>
				</tbody>
			</table>
		</c:if>
	</security:authorize>

</div>