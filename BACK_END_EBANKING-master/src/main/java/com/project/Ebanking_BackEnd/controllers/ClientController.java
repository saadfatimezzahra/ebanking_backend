package com.project.Ebanking_BackEnd.controllers;

import java.util.*;

import javax.validation.Valid;
import javax.validation.constraints.Min;

import com.project.Ebanking_BackEnd.models.*;
import com.project.Ebanking_BackEnd.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.project.Ebanking_BackEnd.exceptions.AgentNotFoundException;
import com.project.Ebanking_BackEnd.exceptions.BankAccountNotFoundException;
import com.project.Ebanking_BackEnd.payload.request.AddClientRequest;
import com.project.Ebanking_BackEnd.payload.response.MessageResponse;
import com.project.Ebanking_BackEnd.services.ClientOperationsService;
import com.project.Ebanking_BackEnd.services.ClientService;
import com.project.Ebanking_BackEnd.services.EmailServiceImp;
import com.project.Ebanking_BackEnd.services.FactureService;
import com.project.Ebanking_BackEnd.services.UserService;
import com.project.Ebanking_BackEnd.services.sms.Service;
import com.project.Ebanking_BackEnd.services.sms.SmsRequest;

@RestController
@RequestMapping("/api/client")
@CrossOrigin(origins= "http://localhost:4200/")
public class ClientController {
	@Autowired
    private BankAccountRepository bankAccountRepository;
	@Autowired
    AccountOperationRepository accountOperationRepository;
	@Autowired
    ClientService serv;
	@Autowired
	ClientOperationsService clietOpServ;
	@Autowired
	UserRepository userRepository;
	 
	@Autowired
	ClientRepository repo;

	@Autowired
	RoleRepository roleRepository;

	@Autowired
	PasswordEncoder encoder;
	  
	@Autowired
	EmailServiceImp emailService;
	  
	@Autowired
	UserService user_serv;
	
	@Autowired
	FactureService fact_serv;

	@Autowired
	Service service;
	  
    @Autowired
    public ClientController(ClientService serv) {
        this.serv = serv;
    }
    
    @GetMapping(value="/clients")
    public List<Client> getAllClients(){
        return serv.getAllClients();
    }           
    @GetMapping(value="/clients/{firstname}")
    public Client getClientById(@PathVariable("firstname") @Min(1) int id) {
   	 Client std = serv.findById(id)
                                    .orElseThrow(()->new AgentNotFoundException("Client with "+id+" is Not Found!"));
        return std;
    } 
    @PostMapping("/addClient")
    public ResponseEntity<?> registerUser(@Valid @RequestBody AddClientRequest signUpRequest) {
      /*if (userRepository.existsByUsername(signUpRequest.getUsername())) {
        return ResponseEntity.badRequest().body(new MessageResponse("Error: Username is already taken!"));
      }*/

      if (repo.existsByEmail(signUpRequest.getEmail())) {
        return ResponseEntity.badRequest().body(new MessageResponse("Error: Email is already in use!"));
      }

      //Generate password 
      char[] password= User.generatePassword();      
      System.out.println(password);
      System.out.println(String.valueOf(password));
      
     Client user1 = new Client(signUpRequest.getFirstname(),signUpRequest.getLastname(),signUpRequest.getPhone(),signUpRequest.getAddress(),signUpRequest.getDateOfBirth(),
                           signUpRequest.getEmail(),signUpRequest.getConfirmationEmail(),signUpRequest.getPieceIdentity(),signUpRequest.getN_pieceIdentity()
                         );

        repo.save(user1);
        //we can retrieve an id for example , after the user has been registred
        System.out.println(user1.getId());

        User user = new User(signUpRequest.getFirstname(),signUpRequest.getLastname(),
             signUpRequest.getEmail(),
             encoder.encode(java.nio.CharBuffer.wrap(password)),user1);
     
     Set<Role> roles = new HashSet<>();
     Role modRole = roleRepository.findByName(ERole.ROLE_CLIENT)
             .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
         roles.add(modRole);
         
       
      //EmailServiceImp emailService = new EmailServiceImp();
     
      emailService.sendEmail(String.valueOf(password),signUpRequest.getEmail());
        
      user.setRoles(roles);
      userRepository.save(user);

        Compte compte = new Compte(Double.valueOf(signUpRequest.getBalance()), new Date() , AccountStatus.CREATED , user1);
        bankAccountRepository.save(compte);
      return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
    }
   
    
    @PutMapping("/update_client/{id}")
    public Client updateClient(@PathVariable("id") @Min(1) int id, @Valid @RequestBody Client newstd) {
   	 Client stdu = serv.findById(id)
                                     .orElseThrow(()->new AgentNotFoundException("Client with "+id+" is Not Found!"));
        stdu.setFirstname(newstd.getFirstname());
        stdu.setLastname(newstd.getLastname());
        stdu.setPhone(newstd.getPhone());
        stdu.setEmail(newstd.getEmail());
        stdu.setConfirmationEmail(newstd.getConfirmationEmail());
        stdu.setAddress(newstd.getAddress());
        stdu.setDateOfBirth(newstd.getDateOfBirth());
        
        User user = user_serv.findByClient(stdu);
        user.setFirstname(newstd.getFirstname());
        user.setLastname(newstd.getLastname());
        user.setEmail(newstd.getEmail());
        
        userRepository.save(user); 

        
        return serv.save(stdu);   
    }
    
    @DeleteMapping(value="/clients/{id}")
    public String deleteClient(@PathVariable("id") @Min(1) int id) {
   	 Client std = serv.findById(id)
                                     .orElseThrow(()->new AgentNotFoundException("Client with "+id+" is Not Found!"));
   	 serv.deleteById(std.getId());
        return "Client with ID :"+id+" is deleted";            
    }


    //add an amount to an account
    @PostMapping("/credit/{accountId}/{amount}")
    public void credit(@PathVariable String accountId,@PathVariable double amount) throws BankAccountNotFoundException {
        Compte bankAccount=bankAccountRepository.findById(accountId)
                .orElseThrow(()->new BankAccountNotFoundException("BankAccount not found"));
        AccountOperation accountOperation=new AccountOperation();
        accountOperation.setType(OperationType.CREDIT);
        accountOperation.setAmount(amount);
        accountOperation.setOperationDate(null);
        accountOperation.setBankaccount(bankAccount);
        accountOperationRepository.save(accountOperation);
        bankAccount.setBalance(bankAccount.getBalance()+amount);
        bankAccountRepository.save(bankAccount);
    }

	
	@GetMapping("/Historique/{id}")
	public List<AccountOperation> findByBankAccountId(@PathVariable int id) {
		
        return clietOpServ.find(id);
    }

    @GetMapping("/{userId}")
    public Client userDetails(@PathVariable int userId){

        System.out.println(userId);
        Client client = userRepository.findClientbyUserId(userId);// this user have a client role
        System.out.println(client);
        return client;
    }
    @GetMapping("/compte/{clientId}")
    public Compte compteDetails(@PathVariable int clientId){

        Compte compte = bankAccountRepository.findByCustomerId(clientId);
        System.out.println(compte.getId());
        return compte;
    }
	  
	/*@GetMapping("/getImpayed")
	  public String getImpayed() {
		this.getInfos();
		return null;
	  }
	  */
	@GetMapping("/payed/{id}")
	public List<Facture> find(@PathVariable int id)
	{
		return fact_serv.find(id);
	 }
	
	@GetMapping("/facture/{id}")
	public Facture findFacture(@PathVariable int id)
	{

        return fact_serv.findById(id);
	 }
   
	@GetMapping("/updateFacture/{id}/{factureid}")
	public int updateFacture(@PathVariable int id,@PathVariable int factureid)
	{
		return fact_serv.update(id,factureid);
	}
	
	@GetMapping("/checkSolde/{id}/{factureid}")
	public Boolean checkSolde(@PathVariable int id,@PathVariable int factureid)
	{
        return fact_serv.checkSolde(id, factureid);
	}
	
	@PostMapping("/sendSMS")
    public void sendSms( @Valid @RequestBody SmsRequest smsRequest){

        System.out.println("SMMMMMS");

        service.sendSms(smsRequest);

    }
	@GetMapping("/validPay/{id}/{factureid}")
    //id c'est celui du compte
    public ResponseEntity<?> validPay(@PathVariable int id,@PathVariable int factureid){
        fact_serv.validPay(id,factureid);
        return  new ResponseEntity<>(HttpStatus.OK);
    }
}
