package com.skillswap.service;

import com.skillswap.dao.DAOFactory;
import com.skillswap.dao.UserDAO;
import com.skillswap.exception.DataAccessException;
import com.skillswap.model.Skill;
import com.skillswap.model.Student;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Finds learning partners for a student:
 * A match is another student who TEACHES something this student wants to LEARN.
 * Mutual matches (both can teach each other) rank highest, followed by average rating.
 */
public class MatchingService {

    private final UserDAO userDAO;

    public MatchingService() {
        this(DAOFactory.getInstance().getUserDAO());
    }

    public MatchingService(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    public static class Match {
        public final Student partner;
        public final List<Skill> theyCanTeachYou;
        public final List<Skill> youCanTeachThem;

        public Match(Student partner, List<Skill> theyCanTeachYou, List<Skill> youCanTeachThem) {
            this.partner = partner;
            this.theyCanTeachYou = theyCanTeachYou;
            this.youCanTeachThem = youCanTeachThem;
        }

        public boolean isMutual() {
            return !youCanTeachThem.isEmpty();
        }
    }

    public List<Match> findMatchesFor(Student me) throws DataAccessException {
        return findMatchesFor(me, userDAO.findAllStudents());
    }

    public List<Match> findMatchesFor(Student me, List<Student> allStudents) {
        List<Match> matches = new ArrayList<>();
        if (me == null || allStudents == null) return matches;

        for (Student other : allStudents) {
            if (other.getUserId() == me.getUserId()) continue;

            List<Skill> theyTeachIWantToLearn = new ArrayList<>();
            for (Skill wanted : me.getLearnSkills().keySet()) {
                if (other.getTeachSkills().containsKey(wanted)) {
                    theyTeachIWantToLearn.add(wanted);
                }
            }
            if (theyTeachIWantToLearn.isEmpty()) continue; // No match for me

            List<Skill> iTeachTheyWantToLearn = new ArrayList<>();
            for (Skill wanted : other.getLearnSkills().keySet()) {
                if (me.getTeachSkills().containsKey(wanted)) {
                    iTeachTheyWantToLearn.add(wanted);
                }
            }

            matches.add(new Match(other, theyTeachIWantToLearn, iTeachTheyWantToLearn));
        }

        // Sort mutual matches first, then by partner average rating descending
        matches.sort(Comparator
                .comparing((Match m) -> !m.isMutual())
                .thenComparing(m -> -m.partner.getAverageRating()));

        return matches;
    }
}
