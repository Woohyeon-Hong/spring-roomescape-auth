package roomescape.member.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.auth.annotation.LoginMember;
import roomescape.auth.annotation.RequireAuth;
import roomescape.member.controller.dto.MemberRequest;
import roomescape.member.domain.Member;
import roomescape.member.service.MemberService;

@RestController
@RequestMapping("/members")
public class MemberController {

    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    @PostMapping
    public ResponseEntity<Void> createMember(@RequestBody MemberRequest request) {
        memberService.signUp(request.toCommand());
        return ResponseEntity.noContent().build();
    }

    @RequireAuth
    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteMember(@LoginMember Member member) {
        memberService.signOut(member.getId());
        return ResponseEntity.noContent().build();
    }
}
