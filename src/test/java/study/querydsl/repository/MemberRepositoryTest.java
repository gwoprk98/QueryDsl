package study.querydsl.repository;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.dto.MemberSearchCondition;
import study.querydsl.dto.MemberTeamDto;
import study.querydsl.entity.Member;
import study.querydsl.entity.Team;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
public class MemberRepositoryTest {

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    EntityManager em;

    @Test
    public void basicTest() {
        Member member = new Member("member1", 10);
        memberRepository.save(member);

        Member findMember = memberRepository.findById(member.getId()).get();
        assertThat(findMember).isEqualTo(member);

        List<Member> result1 = memberRepository.findAll();
        assertThat(result1).containsExactly(member);

        List<Member> result2 = memberRepository.findByUsername("member1");
        assertThat(result2).containsExactly(member);

        List<Member> result3 = memberRepository.findAll();
        assertThat(result1).containsExactly(member);

        List<Member> result4 = memberRepository.findByUsername("member1");
        assertThat(result2).containsExactly(member);
    }

    @Test
    @Transactional // 변경 감지를 위해 추가하거나 클래스 레벨에 있어야 함
    void searchTest() {
        // 1. 데이터 생성 시 "teamB" (공백 없음)으로 통일
        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB"); // <- 수정
        em.persist(teamA);
        em.persist(teamB);

        Member member1 = new Member("member1", 10, teamA);
        Member member2 = new Member("member2", 20, teamA);
        Member member3 = new Member("member3", 30, teamB);
        Member member4 = new Member("member4", 40, teamB);
        em.persist(member1);
        em.persist(member2);
        em.persist(member3);
        em.persist(member4);

        MemberSearchCondition condition = new MemberSearchCondition();
        // 2. 검색 조건도 "teamB" (공백 없음)
        condition.setTeamName("teamB"); // <- 일관성 유지
        condition.setAgeGoe(35);
        condition.setAgeLoe(40);

        List<MemberTeamDto> result = memberRepository.search(condition);

        // 3. 기대값도 실제 결과인 "member4"로 수정
        assertThat(result).extracting("username")
                .containsExactly("member4"); // <- 수정
    }

    @Test
    @Transactional // 변경 감지를 위해 추가하거나 클래스 레벨에 있어야 함
    void searchPageSimpleTest() {
        // 1. 데이터 생성 시 "teamB" (공백 없음)으로 통일
        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB"); // <- 수정
        em.persist(teamA);
        em.persist(teamB);

        Member member1 = new Member("member1", 10, teamA);
        Member member2 = new Member("member2", 20, teamA);
        Member member3 = new Member("member3", 30, teamB);
        Member member4 = new Member("member4", 40, teamB);
        em.persist(member1);
        em.persist(member2);
        em.persist(member3);
        em.persist(member4);

        MemberSearchCondition condition = new MemberSearchCondition();
        PageRequest pageRequest = PageRequest.of(0, 3);

        Page<MemberTeamDto> result = memberRepository.searchPageSimple(condition, pageRequest);

        assertThat(result.getSize()).isEqualTo(3);
        assertThat(result.getContent()).extracting("username").containsExactly("member1", "member2", "member3");
    }
}
