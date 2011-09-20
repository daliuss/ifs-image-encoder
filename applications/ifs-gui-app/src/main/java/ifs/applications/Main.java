package ifs.applications;

import org.apache.log4j.Logger;
import org.springframework.context.support.GenericXmlApplicationContext;
import org.springframework.beans.factory.BeanFactory;

public class Main {
	
	public static final Logger log = Logger.getLogger(Main.class);
	
	public static final String BEAN_DEFINITION_XML = "classpath:META-INF/spring/*-beans.xml";
	public static final String MENU_BEAN = "menu";
	
	private static BeanFactory beanFactoryInstance;
		
	static public void main(String[] args) {
		initializeObjects();
	}
	
	static void initializeObjects() {
		MainApplicationController menu = (MainApplicationController)getBeanFactory().getBean(MENU_BEAN);
		menu.display();
	}
	
	static BeanFactory getBeanFactory() {
		if (beanFactoryInstance == null) {
			GenericXmlApplicationContext ctx = new GenericXmlApplicationContext();
			ctx.load(BEAN_DEFINITION_XML);
			ctx.refresh();
			beanFactoryInstance = (BeanFactory)ctx;
		}
		return beanFactoryInstance;
	}
}
