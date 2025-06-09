NFR ID	Category	Requirement Description	Priority	Status	Verification Method	Notes
NFR-1.1	Performance	The application shall load initial screens within 3 seconds on standard mobile devices.	Medium	Not Verified	Load testing	
NFR-1.2	Performance	The application shall process form submissions within 2 seconds under normal network conditions.	Medium	Not Verified	Performance testing	
NFR-1.3	Performance	The application shall support at least 100 concurrent users without performance degradation.	Medium	Not Verified	Load testing	
NFR-1.4	Performance	The application shall maintain responsiveness when operating with poor connectivity.	Medium	Not Verified	Field testing	
NFR-2.1	Usability	The application shall be usable by operators with minimal technical skills.	Medium	Not Verified	User testing	
NFR-2.2	Usability	The application shall provide a bilingual interface (Spanish/English).	Medium	Not Verified	Manual inspection	
NFR-2.3	Usability	The application shall follow mobile design best practices for intuitive navigation.	Medium	Not Verified	UX review	
NFR-2.4	Usability	The application shall minimize the number of taps required to complete common tasks.	Medium	Not Verified	UX review	
NFR-3.1	Reliability	The application shall have an uptime of at least 99.5%.	Medium	Not Verified	Uptime monitoring	
NFR-3.2	Reliability	The application shall preserve user input in case of unexpected closures.	Medium	Not Verified	Crash testing	
NFR-3.3	Reliability	The application shall gracefully handle network connectivity issues.	Medium	Not Verified	Network simulation testing	
NFR-4.1	Security	The application shall encrypt all data in transit using TLS 1.2 or higher.	Medium	Verified	Security audit	
NFR-4.2	Security	The application shall securely store authentication credentials.	Medium	In Progress	Security audit	
NFR-4.3	Security	The application shall implement session timeouts after 30 minutes of inactivity.	Medium	Not Verified	Manual testing	
NFR-5.1	Compatibility	The application shall function on Android devices running version 8.0 and higher.	Medium	Not Verified	Device testing	
NFR-5.2	Compatibility	The application shall function on iOS devices running version 13.0 and higher.	Medium	Not Verified	Device testing	
NFR-5.3	Compatibility	The application shall be responsive on screens ranging from 4.7" to 12.9".	Medium	Not Verified	Device testing	
NFR-6.1	Scalability	The application shall support growth to at least 1,000 customers without architectural changes.	Medium	Not Verified	Architecture review	
NFR-6.2	Scalability	The application shall support at least 10,000 vehicles across all customers.	Medium	Not Verified	Architecture review	