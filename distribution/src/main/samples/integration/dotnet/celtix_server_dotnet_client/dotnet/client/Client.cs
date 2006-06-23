using System;

namespace HelloWorldClient
{
	class Client
	{
		static void Main(string[] args)
		{
			try 
			{
				HelloWorldService service = new HelloWorldService();
      
				Console.WriteLine("Hello World dotnet Client");
				Console.WriteLine("");
			
				string str_in, str_out;
				
				str_out = service.sayHi();
				Console.WriteLine("sayHi method returned: " + str_out);

				if(args.Length >= 1)
				{
					str_in = args[0];
				}
				else
				{
					str_in = "Early Adopter";
				}
			
				str_out = service.greetMe(str_in);
				Console.WriteLine("greetMe method returned: " + str_out);
			}
			catch (System.Exception ex) 
			{
				Console.WriteLine(ex.Message);
			}
		}
	}
}
