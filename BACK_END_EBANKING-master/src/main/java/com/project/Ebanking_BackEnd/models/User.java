package com.project.Ebanking_BackEnd.models;

import java.sql.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import org.springframework.security.core.GrantedAuthority;

import javax.persistence.InheritanceType;



@Entity
@Table(name = "users")
/*@Inheritance( strategy = InheritanceType.SINGLE_TABLE )
@DiscriminatorColumn( name="discriminator", discriminatorType = DiscriminatorType.INTEGER )
@DiscriminatorValue("0")*/
@Inheritance(strategy=InheritanceType.TABLE_PER_CLASS)
//@Inheritance(strategy=InheritanceType.TABLE_PER_CLASS)
public class User {

	private static final long serialVersionUID = 1L;

	 @Id
	 @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
	
	/*
	 * Commons Attributes
	 */
	@NotBlank
	@Size(max = 30)
	protected String firstname;

	@NotBlank
	@Size(max = 30)
	protected String lastname;

	/*@NotBlank
	@Size(max = 30)
	protected String phone;*/

	/*@NotBlank
	@Size(max = 30)
	protected String address;*/
	//protected Date dateOfBirth;
	protected int has_Already_loggedIn;
	
	 @Email
	protected String email;
	 
	 //@Column(name="passwordGenerated")
	 protected String password;

	/*@NotBlank
	 @Size(max = 120)*/
	 //protected String confirmationEmail;
	
      @ManyToMany(fetch = FetchType.LAZY)
      @JoinTable(name = "user_roles", 
                 joinColumns = @JoinColumn(name = "user_id"),
                 inverseJoinColumns = @JoinColumn(name = "role_id"))
	protected Set<Role> roles = new HashSet<>();

     @OneToOne
     @JoinColumn(name="agent_id")
     protected Agent agent;
     @OneToOne
     @JoinColumn(name="client_id")
     protected Client client;
     @OneToOne
     @JoinColumn(name="admin_id")
     protected Admin admin;

      
      
	
	public Client getClient() {
		return client;
	}


	public void setClient(Client client) {
		this.client = client;
	}


	public User(@NotBlank @Size(max = 30) String firstname, @NotBlank @Size(max = 30) String lastname,
			@Email String email,String pass) {
		
		super();
		this.firstname = firstname;
		this.lastname = lastname;
		this.has_Already_loggedIn = 0;
		this.email = email;
		this.password=pass;
		/*Role role = new Role(ERole.ROLE_ADMIN);
		this.roles.add(role);*/	
		}
	

	
	//for Client
	public User(@NotBlank @Size(max = 30) String firstname, @NotBlank @Size(max = 30) String lastname,
			@Email String email, String password,Client client) {
		super();
		this.firstname = firstname;
		this.lastname = lastname;
		this.email = email;
		this.password = password;
		this.client=client;
		this.has_Already_loggedIn = 0;
		Role role = new Role(ERole.ROLE_CLIENT);
		this.roles.add(role);
	}
	
	//for agent
	public User(@NotBlank @Size(max = 30) String firstname, @NotBlank @Size(max = 30) String lastname,
			@Email String email, String password,Agent agent) {
		super();
		this.firstname = firstname;
		this.lastname = lastname;
		this.email = email;
		this.password = password;
		this.agent=agent;
		this.has_Already_loggedIn = 0;
		Role role = new Role(ERole.ROLE_AGENT);
		this.roles.add(role);
	}
	
	//for Admin
		public User(@NotBlank @Size(max = 30) String firstname, @NotBlank @Size(max = 30) String lastname,
				@Email String email, String password,Admin admin) {
			super();
			this.firstname = firstname;
			this.lastname = lastname;
			this.email = email;
			this.password = password;
			this.admin=admin;
			this.has_Already_loggedIn = 0;
			Role role = new Role(ERole.ROLE_AGENT);
			this.roles.add(role);
		}

	


	public User(int id, @NotBlank @Size(max = 30) String firstname, @NotBlank @Size(max = 30) String lastname,
			@Email String email, String password, Set<Role> roles) {
		super();
		this.id = id;
		this.firstname = firstname;
		this.lastname = lastname;
		this.email = email;
		this.password = password;
		this.roles = roles;
	}


	public User(@NotBlank @Size(max = 30) String firstname, @NotBlank @Size(max = 30) String lastname, @Email String email, String password, Set<Role> roles) {
		super();
		this.firstname = firstname;
		this.lastname = lastname;
		this.email = email;
		this.password = password;
		this.roles = roles;
	}



	


	public User() {
		super();
		// TODO Auto-generated constructor stub
	}


	public int getId() {
		return id;
	}


	public void setId(int id) {
		this.id = id;
	}


	public String getFirstname() {
		return firstname;
	}


	public void setFirstname(String firstname) {
		this.firstname = firstname;
	}


	public String getLastname() {
		return lastname;
	}


	public void setLastname(String lastname) {
		this.lastname = lastname;
	}


	public void setHas_Already_loggedIn(int has_Already_loggedIn) {
		this.has_Already_loggedIn = has_Already_loggedIn;
	}


	public String getEmail() {
		return email;
	}


	public void setEmail(String email) {
		this.email = email;
	}


	public String getPassword() {
		return password;
	}


	public void setPassword(String password) {
		this.password = password;
	}


	
	public Set<Role> getRoles() {
		return roles;
	}


	public void setRoles(Set<Role> roles) {
		this.roles = roles;
	}
      
	public int getHas_Already_loggedIn() {
		return has_Already_loggedIn;
	}
	
	
	public static char[] generatePassword() {
		
        String capitalCaseLetters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String lowerCaseLetters = "abcdefghijklmnopqrstuvwxyz";
        String specialCharacters = "!@#$";
        String numbers = "1234567890";
        String combinedChars = capitalCaseLetters + lowerCaseLetters + specialCharacters + numbers;
        Random random = new Random();
        char[] password = new char[6];

        password[0] = lowerCaseLetters.charAt(random.nextInt(lowerCaseLetters.length()));
        password[1] = capitalCaseLetters.charAt(random.nextInt(capitalCaseLetters.length()));
        password[2] = specialCharacters.charAt(random.nextInt(specialCharacters.length()));
        password[3] = numbers.charAt(random.nextInt(numbers.length()));
     
        for(int i = 4; i< 6 ; i++) {
           password[i] = combinedChars.charAt(random.nextInt(combinedChars.length()));
        }
       
        return password;
     }


	public Agent getAgent() {
		return agent;
	}


	public void setAgent(Agent agent) {
		this.agent = agent;
	}


	public Admin getAdmin() {
		return admin;
	}


	public void setAdmin(Admin admin) {
		this.admin = admin;
	}


	


	/*
	public static String generatePassword() {
		StringBuilder token = new StringBuilder();

		return token.append(UUID.randomUUID().toString())
				.append(UUID.randomUUID().toString()).toString();
	}
*/
      
}

