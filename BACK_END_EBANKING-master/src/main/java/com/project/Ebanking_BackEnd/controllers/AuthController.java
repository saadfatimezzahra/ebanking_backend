package com.project.Ebanking_BackEnd.controllers;

import java.security.Principal;
import java.sql.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.validation.Valid;
import javax.validation.constraints.Email;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import com.project.Ebanking_BackEnd.payload.request.FirstLoginRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.project.Ebanking_BackEnd.exceptions.AgentNotFoundException;
import com.project.Ebanking_BackEnd.models.Admin;
import com.project.Ebanking_BackEnd.models.Agent;
import com.project.Ebanking_BackEnd.models.Client;
import com.project.Ebanking_BackEnd.models.ERole;
import com.project.Ebanking_BackEnd.models.Role;
import com.project.Ebanking_BackEnd.models.User;
import com.project.Ebanking_BackEnd.payload.request.ChangePasswordRequest;
import com.project.Ebanking_BackEnd.payload.request.LoginRequest;
import com.project.Ebanking_BackEnd.payload.request.SignupRequest;
import com.project.Ebanking_BackEnd.payload.response.JwtResponse;
import com.project.Ebanking_BackEnd.payload.response.MessageResponse;
import com.project.Ebanking_BackEnd.payload.response.UserInfoResponse;
import com.project.Ebanking_BackEnd.repository.AdminRepository;
import com.project.Ebanking_BackEnd.repository.RoleRepository;
import com.project.Ebanking_BackEnd.repository.UserRepository;
import com.project.Ebanking_BackEnd.security.jwt.JwtUtils;
import com.project.Ebanking_BackEnd.security.services.UserDetailsImpl;
import com.project.Ebanking_BackEnd.services.AgentService;
import com.project.Ebanking_BackEnd.services.ClientService;
import com.project.Ebanking_BackEnd.services.EmailServiceImp;
import com.project.Ebanking_BackEnd.services.UserService;


@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/api/auth")
public class AuthController {	
  @Autowired
   UserService serv;
  
  
  @Autowired
  AuthenticationManager authenticationManager;

  @Autowired
  UserRepository userRepository;
  
  @Autowired
  AdminRepository repo;

  @Autowired
  RoleRepository roleRepository;

  @Autowired
  PasswordEncoder encoder;
  
  @Autowired
  EmailServiceImp emailService;

  @Autowired
  JwtUtils jwtUtils;
  
  @Autowired
  ClientService client_serv;

  @PostMapping("/signin")
  public ResponseEntity<JwtResponse> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {

    Authentication authentication = authenticationManager
        .authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));

    SecurityContextHolder.getContext().setAuthentication(authentication);

    UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

    String jwt = jwtUtils.generateJwtToken(authentication);

    List<String> roles = userDetails.getAuthorities().stream()
        .map(item -> item.getAuthority())
        .collect(Collectors.toList());
  // return new UserInfoResponse(userDetails.getId(),roles, userDetails.getUser().getAdmin(),userDetails.getUser().getClient() ,userDetails.getUser().getAgent());
    
   

   return ResponseEntity.ok(new JwtResponse(jwt, 
           userDetails.getId(), 
           userDetails.getUsername(), 
           userDetails.getEmail(), 
           roles));
   
   /*return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
        .body(userDetails.getId(),roles);*/
  }

  @PostMapping("/signup")
  public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {

    if (userRepository.existsByEmail(signUpRequest.getEmail())) {
      return ResponseEntity.badRequest().body(new MessageResponse("Error: Email is already in use!"));
    }

    //Generate password 
    char[] password= User.generatePassword();
    Admin user1 = new Admin(signUpRequest.getFirstname(),signUpRequest.getLastname(),signUpRequest.getPhone(),signUpRequest.getAddress(),signUpRequest.getDateOfBirth(),
            signUpRequest.getEmail(),signUpRequest.getConfirmationEmail()
          );

   repo.save(user1);

			
   User user = new User(signUpRequest.getFirstname(),signUpRequest.getLastname(),
                         signUpRequest.getEmail(),
                         encoder.encode(java.nio.CharBuffer.wrap(password)),user1);

    System.out.println(password);    
    
    Set<Role> roles = new HashSet<>();
    Role modRole = roleRepository.findByName(ERole.ROLE_ADMIN)
            .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
        roles.add(modRole);

   
    emailService.sendEmail(String.valueOf(password),signUpRequest.getEmail());
   
    user.setRoles(roles);
    userRepository.save(user);

    return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
  }

  @PostMapping("/signout")
  public ResponseEntity<?> logoutUser() {
    ResponseCookie cookie = jwtUtils.getCleanJwtCookie();
    return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, cookie.toString())
        .body(new MessageResponse("You've been signed out!"));
  }

    @PostMapping(value = "/first")
    public int checkIfAlreagyLoggedIn(@RequestBody FirstLoginRequest request) {

        System.out.println(request.getEmail());
        User user= serv.findByEmail(request.getEmail());
        int check= user.getHas_Already_loggedIn();
        System.out.println(check);

        return check;
    }

    @PostMapping(value = "/change_password")
    public ResponseEntity<String> changePassword(@RequestBody ChangePasswordRequest request) {

        System.out.println(request.getOldPassword());
        User user = serv.findByEmail(request.getOldPassword());
        System.out.println(user.getHas_Already_loggedIn());

        //ResponseEntity.body(new MessageResponse("Don't Change Password"));
        //throw new ResponseStatusException(HttpStatus.FORBIDDEN, "L'ancien mot de passe est incorrect.");
        if (!request.getNewPassword().equals(request.getConfPassword()))
            return ResponseEntity.badRequest().body("Les deux mots de passe ne sont pas identiques.");
        //throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Les deux mots de passe ne sont pas identiques.");
        user.setPassword(encoder.encode(java.nio.CharBuffer.wrap(request.getNewPassword())));
        user.setHas_Already_loggedIn(1);
        System.out.println(user.getPassword());
        userRepository.save(user);


        return ResponseEntity.badRequest().body("Password changed !!");

    }
  @GetMapping("/email")
  public String getEmail() {
	  Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
	  String username;
	  if (principal instanceof UserDetails) {
	     username = ((UserDetails)principal).getUsername();
	  } else {
	    username = principal.toString();
	  }
	  
	  return username;
  }
  

  @GetMapping("/name")
  public Object getName() {
	  Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
	  String name;
	  if (principal instanceof UserDetailsImpl) {
		  name = ((UserDetailsImpl)principal).getName();
	  } else {
		  name = principal.toString();
	  }
	  
	  return name;
  }
  
  @GetMapping("/currentusername")
  public String currentUserName(Principal principal) {
          return principal.getName();
  }
 
    
}
