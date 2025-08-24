//package syu.likealion3.hackathon.support;
//
//import org.junit.jupiter.api.Assumptions;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Component;
//
//import javax.sql.DataSource;
//import java.sql.Connection;
//
///**
// * 실 DB 테스트 안전가드
// * - DB URL에 'test' 문자열이 없으면 테스트를 skip 처리
// * - AWS RDS를 사용할 때는 반드시 테스트 전용 DB 이름에 'test' 포함
// */
//@Component
//public class ProdDbGuard {
//
//    @Autowired
//    DataSource dataSource;
//
//    public void assumeSafeTestDatabase() {
//        try (Connection con = dataSource.getConnection()) {
//            String url = con.getMetaData().getURL();
//            Assumptions.assumeTrue(
//                    url != null && url.toLowerCase().contains("test"),
//                    () -> "안전가드 발동: DB URL에 'test'가 없습니다. url=" + url
//            );
//        } catch (Exception e) {
//            throw new RuntimeException("DB 가드 점검 실패", e);
//        }
//    }
//}
