package com.skillswap.dao.inmemory;

import com.skillswap.dao.RequestDAO;
import com.skillswap.exception.DataAccessException;
import com.skillswap.model.ExchangeRequest;
import com.skillswap.model.RequestStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * In-memory implementation of RequestDAO.
 */
public class InMemoryRequestDAO implements RequestDAO {

    private final List<ExchangeRequest> requests = new ArrayList<>();
    private final AtomicInteger nextId = new AtomicInteger(1);

    @Override
    public synchronized ExchangeRequest findById(int requestId) throws DataAccessException {
        for (ExchangeRequest r : requests) {
            if (r.getRequestId() == requestId) return r;
        }
        return null;
    }

    @Override
    public synchronized List<ExchangeRequest> findAll() throws DataAccessException {
        return new ArrayList<>(requests);
    }

    @Override
    public synchronized List<ExchangeRequest> findIncomingFor(int studentId) throws DataAccessException {
        List<ExchangeRequest> result = new ArrayList<>();
        for (ExchangeRequest r : requests) {
            if (r.getReceiver() != null && r.getReceiver().getUserId() == studentId) {
                result.add(r);
            }
        }
        return result;
    }

    @Override
    public synchronized List<ExchangeRequest> findOutgoingFor(int studentId) throws DataAccessException {
        List<ExchangeRequest> result = new ArrayList<>();
        for (ExchangeRequest r : requests) {
            if (r.getSender() != null && r.getSender().getUserId() == studentId) {
                result.add(r);
            }
        }
        return result;
    }

    @Override
    public synchronized ExchangeRequest save(ExchangeRequest request) throws DataAccessException {
        if (request == null) throw new DataAccessException("Request cannot be null");
        if (request.getRequestId() <= 0) {
            request.setRequestId(nextId.getAndIncrement());
        }
        requests.removeIf(r -> r.getRequestId() == request.getRequestId());
        requests.add(request);
        return request;
    }

    @Override
    public synchronized void updateStatus(int requestId, RequestStatus status) throws DataAccessException {
        ExchangeRequest req = findById(requestId);
        if (req != null) {
            req.setStatus(status);
        }
    }
}
