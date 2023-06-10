package com.project.Ebanking_BackEnd.repository;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.project.Ebanking_BackEnd.models.Agent;
import com.project.Ebanking_BackEnd.models.Client;
import com.project.Ebanking_BackEnd.models.User;


@Repository
public interface UserRepository extends JpaRepository<User, Long> {
  //Optional<User> findByUsername(String username);
  User findByEmail(String mail);
  //Boolean existsByUsername(String username);

  Boolean existsByEmail(String email);
  User findById(int id);
  User findByPassword(String password);
  @Query("select u.agent from User u where u.id = ?1")
  Agent findAgentbyUserId(int id);
  @Query("select u.client from User u where u.id = ?1")
  Client findClientbyUserId(int id);
  User findByClient(Client stdu);

  User findByAgent(Agent stdu);
 
}
