// Copyright (c) 2000-2003 Quadralay Corporation.  All rights reserved.
//

function  WWHTabs_Object(ParamPanels)
{
  this.mWidth = null;

  this.fReload   = WWHTabs_Reload;
  this.fHeadHTML = WWHTabs_HeadHTML;
  this.fBodyHTML = WWHTabs_BodyHTML;
  this.fLoaded   = WWHTabs_Loaded;

  // Calculate width based on number of panels
  //
  if (ParamPanels > 0)
  {
    this.mWidth = "" + (100 / ParamPanels) + "%";
  }
}

function  WWHTabs_Reload()
{
  WWHFrame.WWHHelp.fReplaceLocation("WWHTabsFrame", WWHFrame.WWHHelp.mHelpURLPrefix + "wwhelp/wwhimpl/js/html/tabs.htm");
}

function  WWHTabs_HeadHTML()
{
  var  StylesHTML = "";


  // Generate style section
  //
  StylesHTML += "<style type=\"text/css\">\n";
  StylesHTML += " <!--\n";
  StylesHTML += "  a.active\n";
  StylesHTML += "  {\n";
  StylesHTML += "    text-decoration: none;\n";
  StylesHTML += "    color: " + WWHFrame.WWHJavaScript.mSettings.mTabs.mSelectedTabTextColor + ";\n";
  StylesHTML += "    " + WWHFrame.WWHJavaScript.mSettings.mTabs.mFontStyle + ";\n";
  StylesHTML += "  }\n";
  StylesHTML += "  a.inactive\n";
  StylesHTML += "  {\n";
  StylesHTML += "    text-decoration: none;\n";
  StylesHTML += "    color: " + WWHFrame.WWHJavaScript.mSettings.mTabs.mDefaultTabTextColor + ";\n";
  StylesHTML += "    " + WWHFrame.WWHJavaScript.mSettings.mTabs.mFontStyle + ";\n";
  StylesHTML += "  }\n";
  StylesHTML += "  th\n";
  StylesHTML += "  {\n";
  StylesHTML += "    color: " + WWHFrame.WWHJavaScript.mSettings.mTabs.mSelectedTabTextColor + ";\n";
  StylesHTML += "    " + WWHFrame.WWHJavaScript.mSettings.mTabs.mFontStyle + ";\n";
  StylesHTML += "  }\n";
  StylesHTML += "  td\n";
  StylesHTML += "  {\n";
  StylesHTML += "    color: " + WWHFrame.WWHJavaScript.mSettings.mTabs.mDefaultTabTextColor + ";\n";
  StylesHTML += "    " + WWHFrame.WWHJavaScript.mSettings.mTabs.mFontStyle + ";\n";
  StylesHTML += "  }\n";
  StylesHTML += " -->\n";
  StylesHTML += "</style>\n";

  return StylesHTML;
}

function  WWHTabs_BodyHTML()
{
  var  TabsHTML = "";
  var  Height = 21;
  var  MaxIndex;
  var  Index;
  var  VarTabTitle;
  var  VarAccessibilityTitle = "";
  var  CellType;
  var  BorderColor;
  var  BackgroundColor;
  var  WrapPrefix;
  var  WrapSuffix;
  var  OnClick;


  // Setup table for tab display
  //
  TabsHTML += "<table border=0 cellspacing=2 cellpadding=0 width=\"100%\">\n";
  TabsHTML += "<tr>\n";

  for (MaxIndex = WWHFrame.WWHJavaScript.mPanels.mPanelEntries.length, Index = 0 ; Index < MaxIndex ; Index++)
  {
    // Get tab title
    //
    VarTabTitle = WWHFrame.WWHJavaScript.mPanels.mPanelEntries[Index].mPanelObject.mPanelTabTitle;

    // Display anchor only if not selected
    //
    if (Index == WWHFrame.WWHJavaScript.mCurrentTab)
    {
      // Determine title for accessibility
      //
      if (WWHFrame.WWHHelp.mbAccessible)
      {
        VarAccessibilityTitle = WWHStringUtilities_FormatMessage(WWHFrame.WWHJavaScript.mMessages.mAccessibilityActiveTab,
                                                                 VarTabTitle);
        VarAccessibilityTitle = " title=\"" + WWHStringUtilities_EscapeHTML(VarAccessibilityTitle) + "\"";
      }

      CellType = "th";
      BorderColor = WWHFrame.WWHJavaScript.mSettings.mTabs.mSelectedTabBorderColor;
      BackgroundColor = WWHFrame.WWHJavaScript.mSettings.mTabs.mSelectedTabColor;
      WrapPrefix = "<b><a class=\"active\" name=\"tab" + Index + "\" href=\"javascript:void(0);\"" + VarAccessibilityTitle + ">";
      WrapSuffix = "</a></b>";
      OnClick = "";
    }
    else
    {
      // Determine title for accessibility
      //
      if (WWHFrame.WWHHelp.mbAccessible)
      {
        VarAccessibilityTitle = WWHStringUtilities_FormatMessage(WWHFrame.WWHJavaScript.mMessages.mAccessibilityInactiveTab,
                                                                 VarTabTitle);
        VarAccessibilityTitle = " title=\"" + WWHStringUtilities_EscapeHTML(VarAccessibilityTitle) + "\"";
      }

      CellType = "td";
      BorderColor = WWHFrame.WWHJavaScript.mSettings.mTabs.mDefaultTabBorderColor;
      BackgroundColor = WWHFrame.WWHJavaScript.mSettings.mTabs.mDefaultTabColor;
      WrapPrefix = "<b><a class=\"inactive\" name=\"tab" + Index + "\" href=\"javascript:WWHFrame.WWHJavaScript.fClickedChangeTab(" + Index + ");\"" + VarAccessibilityTitle + ">";
      WrapSuffix = "</a></b>";
      OnClick = " onClick=\"WWHFrame.WWHJavaScript.fClickedChangeTabWithDelay(" + Index + ");\"";
    }

    TabsHTML += "<td width=\"" + this.mWidth + "\" bgcolor=\"" + BorderColor + "\">";
    TabsHTML += "<table border=0 cellspacing=1 cellpadding=0 width=\"100%\">";
    TabsHTML += "<tr>";

    TabsHTML += "<" + CellType + " nowrap align=center height=" + Height + " width=\"" + this.mWidth + "\" bgcolor=\"" + BackgroundColor + "\"" + OnClick + ">";
    TabsHTML += WrapPrefix;
    TabsHTML += VarTabTitle;
    TabsHTML += WrapSuffix;
    TabsHTML += "</" + CellType + ">";

    TabsHTML += "</tr>";
    TabsHTML += "</table>";
    TabsHTML += "</td>\n";
  }

  TabsHTML += "</tr>\n";
  TabsHTML += "</table>\n";

  return TabsHTML;
}

function  WWHTabs_Loaded()
{
  // Set frame name for accessibility
  //
  if (WWHFrame.WWHHelp.mbAccessible)
  {
    WWHFrame.WWHHelp.fSetFrameName("WWHTabsFrame");
  }

  // Display requested panel
  //
  WWHFrame.WWHJavaScript.mPanels.fChangePanel(WWHFrame.WWHJavaScript.mCurrentTab);
}
