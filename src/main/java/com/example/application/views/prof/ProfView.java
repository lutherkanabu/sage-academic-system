package com.example.application.views.prof;

import com.example.application.data.SamplePerson;
import com.example.application.services.SamplePersonService;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.data.VaadinSpringDataHelpers;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;

@PageTitle("Prof")
@Route("my-view")
@Menu(order = 1, icon = "line-awesome/svg/pencil-ruler-solid.svg")
@RolesAllowed("USER")
@Uses(Icon.class)
public class ProfView extends Composite<VerticalLayout> {

    public ProfView() {
        Tabs tabs = new Tabs();
        Grid basicGrid = new Grid(SamplePerson.class);
        getContent().setWidth("100%");
        getContent().getStyle().set("flex-grow", "1");
        getContent().setAlignSelf(FlexComponent.Alignment.START, tabs);
        tabs.setWidth("100%");
        setTabsSampleData(tabs);
        basicGrid.setWidth("100%");
        basicGrid.getStyle().set("flex-grow", "0");
        setGridSampleData(basicGrid);
        getContent().add(tabs);
        getContent().add(basicGrid);
    }

    private void setTabsSampleData(Tabs tabs) {
        tabs.add(new Tab("Dashboard"));
        tabs.add(new Tab("Payment"));
        tabs.add(new Tab("Shipping"));
    }

    private void setGridSampleData(Grid grid) {
        grid.setItems(query -> samplePersonService.list(
                PageRequest.of(query.getPage(), query.getPageSize(), VaadinSpringDataHelpers.toSpringDataSort(query)))
                .stream());
    }

    @Autowired()
    private SamplePersonService samplePersonService;
}
