package com.cricklocal.service;

import com.cricklocal.dto.PartnershipResponse;
import com.cricklocal.entity.Innings;
import com.cricklocal.entity.Partnership;
import com.cricklocal.entity.Player;
import com.cricklocal.repository.PartnershipRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PartnershipService {

    private final PartnershipRepository partnershipRepository;

    public PartnershipService(
            PartnershipRepository partnershipRepository) {

        this.partnershipRepository = partnershipRepository;
    }

    public Partnership createPartnership(
            Innings innings,
            Player batterOne,
            Player batterTwo,
            Integer partnershipNumber) {

        Partnership partnership = new Partnership();

        partnership.setInnings(innings);
        partnership.setPartnershipNumber(partnershipNumber);
        partnership.setBatterOne(batterOne);
        partnership.setBatterTwo(batterTwo);
        partnership.setRuns(0);
        partnership.setBalls(0);
        partnership.setActive(true);

        return partnershipRepository.save(partnership);
    }

    public Partnership getActivePartnership(Innings innings) {

        return partnershipRepository
                .findByInningsAndActiveTrue(innings)
                .orElse(null);
    }

    public List<Partnership> getByInnings(Innings innings) {

        return partnershipRepository
                .findByInningsOrderByPartnershipNumberAsc(innings);
    }

    public Partnership save(Partnership partnership) {

    return partnershipRepository.saveAndFlush(partnership);
}

    public int getNextPartnershipNumber(Innings innings) {

            List<Partnership> partnerships =
                    partnershipRepository
                            .findByInningsOrderByPartnershipNumberAsc(innings);

        return partnerships.size() + 1;
    }

    public PartnershipResponse toResponse(
            Partnership partnership) {

        PartnershipResponse response =
                new PartnershipResponse();

        response.setId(partnership.getId());

        response.setInningsId(
                partnership.getInnings().getId());

        response.setPartnershipNumber(
                partnership.getPartnershipNumber());

        response.setBatterOneId(
                partnership.getBatterOne().getId());

        response.setBatterOneName(
                partnership.getBatterOne().getDisplayName());

        response.setBatterTwoId(
                partnership.getBatterTwo().getId());

        response.setBatterTwoName(
                partnership.getBatterTwo().getDisplayName());

        response.setRuns(partnership.getRuns());

        response.setBalls(partnership.getBalls());

        response.setActive(partnership.getActive());

        return response;
    }

    public List<PartnershipResponse> getResponsesByInnings(
            Innings innings) {

        return getByInnings(innings)
                .stream()
                .map(this::toResponse)
                .toList();
    }
}