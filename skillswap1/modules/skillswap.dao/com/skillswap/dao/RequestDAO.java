package com.skillswap.dao;

import com.skillswap.exception.DataAccessException;
import com.skillswap.model.ExchangeRequest;
import com.skillswap.model.RequestStatus;

import java.util.List;

/**
 * Data Access Object interface for ExchangeRequest operations.
 */
public interface RequestDAO {
    ExchangeRequest findById(int requestId) throws DataAccessException;
    List<ExchangeRequest> findAll() throws DataAccessException;
    List<ExchangeRequest> findIncomingFor(int studentId) throws DataAccessException;
    List<ExchangeRequest> findOutgoingFor(int studentId) throws DataAccessException;
    ExchangeRequest save(ExchangeRequest request) throws DataAccessException;
    void updateStatus(int requestId, RequestStatus status) throws DataAccessException;
}
