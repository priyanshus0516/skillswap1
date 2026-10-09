package com.skillswap.service;

import com.skillswap.dao.DAOFactory;
import com.skillswap.dao.RequestDAO;
import com.skillswap.exception.DataAccessException;
import com.skillswap.exception.InvalidRequestException;
import com.skillswap.model.*;
import com.skillswap.util.FileLogger;

import java.util.List;

/**
 * Business logic for sending, responding to, and cancelling exchange requests.
 */
public class RequestService {

    private final RequestDAO requestDAO;

    public RequestService() {
        this(DAOFactory.getInstance().getRequestDAO());
    }

    public RequestService(RequestDAO requestDAO) {
        this.requestDAO = requestDAO;
    }

    public ExchangeRequest sendRequest(Student sender, Student receiver,
                                       Skill skillRequested, Skill skillOffered, String message)
            throws InvalidRequestException, DataAccessException {
        if (sender.getUserId() == receiver.getUserId()) {
            throw new InvalidRequestException("You cannot send an exchange request to yourself.");
        }
        if (!receiver.getTeachSkills().containsKey(skillRequested)) {
            throw new InvalidRequestException(receiver.getName() + " does not teach " + skillRequested.getName() + ".");
        }

        ExchangeRequest req = new ExchangeRequest(0, sender, receiver, skillRequested, skillOffered, message);
        ExchangeRequest saved = requestDAO.save(req);
        FileLogger.log("REQUEST SENT: " + saved);
        return saved;
    }

    public void respond(ExchangeRequest request, Student responder, boolean accept)
            throws InvalidRequestException, DataAccessException {
        if (request.getReceiver().getUserId() != responder.getUserId()) {
            throw new InvalidRequestException("Only the receiver can respond to this request.");
        }
        if (request.getStatus() != RequestStatus.PENDING) {
            throw new InvalidRequestException("This request has already been " + request.getStatus() + ".");
        }

        RequestStatus newStatus = accept ? RequestStatus.ACCEPTED : RequestStatus.REJECTED;
        request.setStatus(newStatus);
        requestDAO.updateStatus(request.getRequestId(), newStatus);
        FileLogger.log("REQUEST " + newStatus + ": #" + request.getRequestId());
    }

    public void cancel(ExchangeRequest request, Student requester)
            throws InvalidRequestException, DataAccessException {
        if (request.getSender().getUserId() != requester.getUserId()) {
            throw new InvalidRequestException("Only the sender can cancel this request.");
        }
        request.setStatus(RequestStatus.CANCELLED);
        requestDAO.updateStatus(request.getRequestId(), RequestStatus.CANCELLED);
        FileLogger.log("REQUEST CANCELLED: #" + request.getRequestId());
    }

    public List<ExchangeRequest> incomingFor(Student student) throws DataAccessException {
        return requestDAO.findIncomingFor(student.getUserId());
    }

    public List<ExchangeRequest> outgoingFor(Student student) throws DataAccessException {
        return requestDAO.findOutgoingFor(student.getUserId());
    }
}
