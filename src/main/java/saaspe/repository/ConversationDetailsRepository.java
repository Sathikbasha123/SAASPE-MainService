package saaspe.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import saaspe.entity.ConversationDetails;

public interface ConversationDetailsRepository extends JpaRepository<ConversationDetails, Integer> {

	@Query(value = "select * from saaspe_conversation_entity a where a.userid=:userId and a.conversationtitle=:conversationTitle and endDate is null", nativeQuery = true)
	ConversationDetails findByUserIdAndConversationTitle(String userId, String conversationTitle);

	@Query("Select a From ConversationDetails a where a.userEmail=:userEmail and a.endDate is null")
	List<ConversationDetails> getConversationByUserEmail(String userEmail);

	@Query("Select a From ConversationDetails a where a.conversationId=:conversationId")
	ConversationDetails findByConversationId(String conversationId);

	@Query("Select a From ConversationDetails a where a.conversationId=:conversationId and a.userEmail =:email")
	ConversationDetails findByconversationDetailsCheckForUser(String conversationId, String email);
}
