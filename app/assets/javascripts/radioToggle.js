(function ($) {
    'use strict';

    VORadioToggle.toggleFieldsBasedOnCheckedRadioButton = function(){

        function showFieldsAssociatedWithSelectedRadiosOnPageLoad() {
            //when page loads show the fields associated with selected radio buttons if any are checked
            var $checkedRadioButtons = $('.radio-button-that-show-hides input[type=radio]:checked');
            if ($checkedRadioButtons && $checkedRadioButtons.length > 0) {
                showHideFieldsBasedOnRadioButtonValue();
            }
        }

        var showHideFieldsBasedOnRadioButtonValue = function () {
            var elementsToShowOrHide = $('[data-hidden-by]');
            if(elementsToShowOrHide.length > 0){
                elementsToShowOrHide.each(function(){
                    var elem = $(this);
                    var hiddenBy = elem.attr('data-hidden-by');
                    var showWhenEquals = elem.attr('data-show-when-value-equals');
                    var $elementToToggle = $(this)[0].hasAttribute('data-hides-this') ? $(this) : $(this).closest('.govuk-form-group');

                    var fieldsThatShowHideThis = hiddenBy.split('|');

                    if(fieldsThatShowHideThis.length > 1){
                        //multiple elements hide this so we need to find fields and values that show/hide
                        var valuesOfFieldsThatShowThis = showWhenEquals.split('|');
                        var show = false;
                        //we will loop through all the fields that show this and their values
                        //then we will see if any are matching a value that shows this, otherwise
                        //we will hide it.
                        for (var i = 0; i < fieldsThatShowHideThis.length; i++) {
                            //get the field name from the list of fields that show this
                            var curr = fieldsThatShowHideThis[i];
                            var $masterEl = $('input[name=' + curr + ']:checked');
                            //get the corresponding value that would show this
                            if (valuesOfFieldsThatShowThis[i] === $masterEl.val()) {
                                //if any field should show this we show it, so break loop.
                                show = true;
                                break;
                            }
                        }
                        if(show){
                            $elementToToggle.removeClass('hidden');
                        } else {
                            $elementToToggle.addClass('hidden');
                        }
                    }else{
                        //a single element hides this
                        var $masterElem = $('input[name=\'' + hiddenBy + '\']:checked');
                        if (showWhenEquals.split('|').indexOf($masterElem.val()) > -1) {
                            $elementToToggle.removeClass('hidden');
                        } else {
                            $elementToToggle.addClass('hidden');
                        }
                    }
                });
            }
        };

        //add hidden class to all form groups containing data-hidden-by INPUT elements
        $('*[data-hidden-by]').closest('.govuk-form-group').addClass('hidden');
        //for form fields that need to be shown/hidden as a group.
        // This is necessary as some in the group are also toggled by items in group itself.
        //the data-hides-this attr is added to the div or fieldset or element that is going to be hidden.
        $('[data-hides-this][data-hidden-by]').addClass('hidden');
        //now run on page load to show any that should be shown based on which radios are already selected
        showFieldsAssociatedWithSelectedRadiosOnPageLoad();
        $(document).on('change', '.radio-button-that-show-hides input[type=radio]' , function () {
            showHideFieldsBasedOnRadioButtonValue();
        });
    };

})(jQuery);
