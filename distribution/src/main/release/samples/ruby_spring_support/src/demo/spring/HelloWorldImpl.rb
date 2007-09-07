
require 'java'

include_class 'demo.spring.HelloWorld'

class HelloWorldImpl
  
  def sayHi(message)
    return  "Hello " + message
  end

end

HelloWorldImpl.new