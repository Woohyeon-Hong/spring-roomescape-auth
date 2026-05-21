package roomescape.member.service;

import org.mindrot.jbcrypt.BCrypt;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.member.domain.Member;
import roomescape.member.exception.DuplicateMemberException;
import roomescape.member.exception.MemberNotFoundException;
import roomescape.member.repository.MemberRepository;

@Service
public class MemberService {

    private final MemberRepository memberRepository;

    public MemberService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @Transactional
    public Member signUp(MemberCommand command) {
        if (memberRepository.existByEmail(command.email())) {
            throw new DuplicateMemberException();
        }

        int hashStrength = 12;
        String hashPassword = BCrypt.hashpw(command.rawPassword(), BCrypt.gensalt(hashStrength));

        Member member = Member.of(command.name(), command.email(), hashPassword);

        try {
            return memberRepository.save(member);
        } catch (DataIntegrityViolationException e) {
            throw new DuplicateMemberException();
        }
    }

    public Member getByEmail(String email) {
        return memberRepository.getByEmail(email);
    }

    public void signOut(Long id) {
        if (!memberRepository.existById(id)) {
            throw new MemberNotFoundException();
        }

        int affectedRow = memberRepository.deleteById(id);
        int nonAffected = 0;

        if (affectedRow == nonAffected) {
            throw new MemberNotFoundException();
        }
    }
}
