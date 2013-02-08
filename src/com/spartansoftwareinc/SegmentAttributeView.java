package com.spartansoftwareinc;

import javax.swing.JTabbedPane;

/**
 * Displays ITS metadata attached to the selected segment in the SegmentView.
 */
public class SegmentAttributeView extends JTabbedPane {
    protected SegmentView segmentView;
    protected SegmentAttributeTableView aggregateTableView;
    protected LanguageQualityIssueTableView lqiTableView;
    protected SegmentAttributeTreeView treeView;
    protected NewLanguageQualityIssueView addLQIView;
    private ITSDetailView itsDetailView;

    private Segment selectedSegment;

    public SegmentAttributeView(ITSDetailView detailView) {
        itsDetailView = detailView;

        aggregateTableView = new SegmentAttributeTableView(this);
        addTab("Doc Stats", aggregateTableView);

        lqiTableView = new LanguageQualityIssueTableView(this);
        addTab("LQI", lqiTableView);

        treeView = new SegmentAttributeTreeView(this);
        addTab("Tree", treeView);

        addLQIView = new NewLanguageQualityIssueView(this);
        addTab("+", addLQIView);
    }

    public void setSegmentView(SegmentView segView) {
        this.segmentView = segView;
    }

    public Segment getSelectedSegment() {
        return this.selectedSegment;
    }
    
    public void setSelectedSegment(Segment seg) {
        this.selectedSegment = seg;
        lqiTableView.setSegment(seg);
        treeView.clearTree();
        if (seg.containsLQI()) { treeView.loadLQI(seg.getLQI()); }
        treeView.expandTree();
        addLQIView.updateSegment();
        itsDetailView.clearDisplay();
    }
    
    public void setSelectedMetadata(ITSMetadata its) {
        itsDetailView.setMetadata(selectedSegment, its);
    }
    
    public void deselectMetadata() {
        itsDetailView.clearDisplay();
    }
}
