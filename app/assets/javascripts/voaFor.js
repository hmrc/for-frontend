(function ($) {
    'use strict';

    VoaFor.service = function () {
        return 'sending-rental-information';
    };

    VoaFor.showHistoryBackLink = function(){
        $('a#history-back').show();
    };

    VoaFor.setHelpGDSClasses = function(){
        var $helpFormWrapper = $('#helpFormWrapper');
        $helpFormWrapper.find('input[type=text]')
            .removeClass('input--fullwidth').removeClass('form-control')
            .addClass('govuk-input').addClass('govuk-!-margin-bottom-3');
        $helpFormWrapper.find('button').removeClass('button').addClass('govuk-button');
        $helpFormWrapper.find('label').removeClass('form-label').addClass('govuk-label');
        $helpFormWrapper.find('h2, p').remove();
    };

    VoaFor.printPage = function () {
        setTimeout(function () {
            if (window.document.queryCommandSupported('print')) {
                window.document.execCommand('print', false, null);
            } else {
                window.focus();
                window.print();
            }
        }, 1000);
    };

    VoaFor.printPageShouldPrintOnLoad = function(){
        if($('div.govuk-grid-column-full.print-your-answers').length > 0){
            window.print();
        }
    };

    VoaFor.addField = function () {
        $(document).on('click', '.add', function (e) {
            e.preventDefault();
            var id = $(this).closest('.form-group').attr('id');
            id = id.substring(0, id.lastIndexOf('_'));
            $(this).closest('.form-group').clone().insertBefore($(this).closest('.form-group'));
            $('.add').not(':last').css('display', 'none');
            $('.add-group').each(function () {
                $(this).attr('id', id + '_' + $(this).index());
                $(this).find('input').attr('id', id + '_' + $(this).index() + '_text');
                $(this).find('input').attr('name', id + '[' + $(this).index() + ']');
                $(this).find('label').attr('for', $(this).find('input').attr('id'));
                $('.add-group:last input').val('');
            });
            $('.add-group:last input').focus();
            $('.remove').css('display', 'inline-block');
        });
    };

    VoaFor.removeField = function () {
        $(document).on('click', '.remove', function (e) {
            e.preventDefault();
            $(this).closest('.add-group').remove();
            if ($('.add-group').length === 1) {
                $('.add-group:first').find('.remove').css('display', 'none');
            }
            $('.add:last').css('display', 'table');
        });
    };

    VoaFor.changeIds = function (container, index) {
        function incrementSectionHeadingNumber($clone) {
            //increment the section heading eg "Sub-let 1" changes to "Sub-let 2"
            var $sectionHeading = $clone.find('h3.section-heading span');
            if ($sectionHeading.length > 0) {
                $sectionHeading.text(index + 1 );
            }

            var $steppedRentHeading = $clone.find('h3.stepped-rent');
            if ($steppedRentHeading.length > 0) {
                $steppedRentHeading.text(window.steppedRentHeadings[index]);
            }
        }
        function changeIdAndLabelId($container, $elem, newIndex) {
            var oldId = $elem.attr('id');
            var newId = oldId.replace(/(\d+)/g, newIndex);
            $elem.attr('id', newId);

            var label = $container.find('label[for=\'' + oldId + '\']');
            if (label !== undefined) {
                if (label.attr('for') !== undefined) {
                    label.attr('for', newId);
                }
            }
        }
        //change id of first child form-group to maintain valid html with no duplicated ids.
        var $container = $(container);
        var $firstChildFormGroup = $container.find('fieldset:first');
        if ($firstChildFormGroup !== undefined) {
            var idAtrr = $firstChildFormGroup.attr('id');
            if (idAtrr !== undefined) {
                var newId = idAtrr.replace(/(\d+)/g, index);
                $firstChildFormGroup.attr('id', newId);
            }
        }
        incrementSectionHeadingNumber($container, index);

        $container.find('input, textarea').not('[type="hidden"]').not('[type="radio"]').each(function () {


            var attrName = $(this).attr('name'),
                s = $(this).closest('.multi-fields-group').attr('id'),
                o = s.substring(0, s.lastIndexOf('_') + 1),
                st = $(this).closest('.multi-fields-group').find('.form-date-dayMonth').attr('id'),
                st2 = $(this).closest('.multi-fields-group').find('[data-intel]').attr('class');

            var attrFormgroupId = $(this).closest('.form-group').attr('id');
            if(attrFormgroupId !== undefined){
                $(this).closest('.form-group').attr('id', attrFormgroupId.replace(/(\d+)/g, index));
            }

            changeIdAndLabelId( $container,$(this), index);

            if (attrName.indexOf('street1') > -1 || attrName.indexOf('street2') > -1) {
                $(this).attr('name', attrName.replace(/\[(\d+)\]/g, '[' + index + ']'));
            } else {
                $(this).attr('name', attrName.replace(/(\d+)/g, index));
            }
            $(this).closest('.multi-fields-group').attr('id', o + index);
            if (st) {
                $(this).closest('.multi-fields-group').find('.form-date-dayMonth').attr('id', st.replace(/(\d+)/g, index));
            }
            if (st2) {
                $(this).closest('.multi-fields-group').find('[data-intel]').attr('class', st2.replace(/(\d+)/g, index));
            }
        });

        $container.find('*[data-hidden-by]').each(function(){
            var dataAttribute = $(this).attr('data-hidden-by');
            $(this).attr('data-hidden-by', dataAttribute.replace(/(\d+)/g, index));

        });
        $container.find('div[data-show-fields-group]').each(function () {
            var dataAttribute = $(this).attr('data-show-fields-group');
            $(this).attr('data-show-fields-group', dataAttribute.replace(/(\d+)/g, index));
        });

        $container.find('input[type="text"]').each(function () {
            var nameAttr = $(this).attr('name');
            $(this).attr('name', nameAttr.replace(/\[(\d+)\]/g, '[' + index + ']'));
        });
        $container.find('input[type="hidden"]').each(function () {
            if (this.hasAttribute('name')) {
                var nameAttr = $(this).attr('name');
                $(this).attr('name', nameAttr.replace(/\[(\d+)\]/g, '[' + index + ']'));
            }
        });
        $container.find('input[type="radio"]').each(function () {

            var s = $(this).closest('.multi-fields-group').attr('id'),
                o = s.substring(0, s.lastIndexOf('_') + 1);

            $(this).attr('name', $(this).attr('name').replace(/(\d+)/g, index));
            changeIdAndLabelId($container, $(this), index);
            var showFieldAttr = 'data-show-fields';
            if (this.hasAttribute(showFieldAttr)) {
                var dataShowFields = $(this).attr(showFieldAttr);
                $(this).attr(showFieldAttr, dataShowFields.replace(/(\d+)/g, index));
            }

            $(this).closest('.multi-fields-group').attr('id', o + index);
        });
    };

    VoaFor.addFieldMulti = function () {

        //remove if js
        $('.deleteifjs').remove();

        $(document).on('click', '.add-multi-fields', function (e) {
            e.preventDefault();
            addMultiFields($(this));
        });

        function addMultiFields(addLink) {
            var element = addLink.closest('fieldset');
            var limit = parseInt(element.attr('data-limit'), 10);
            var minSize = element.attr('data-min-size') ? parseInt(element.attr('data-min-size'), 10) : 1;
            var existingCount = element.find('.multi-fields-group').length;
            //clone the multi-fields-group
            var elementToClone = element.find('.multi-fields-group:last');
            var $clone = elementToClone.clone();
            VoaFor.changeIds($clone, existingCount);
            $clone.insertAfter(elementToClone);
            hideTogglingElementsInClone($clone);
            removeValuesFromClonedInputs($clone);
            removeErrors($clone);
            //remove p's from clone, no idea why.
            $clone.find('p').remove();
            //show the remove button on clone
            if (existingCount >= minSize) {
                $clone.find('a.remove-multi-fields').show();
            }
            element.find('.multi-fields-group:last .chars').text(element.find('.multi-fields-group:last .chars').attr('data-max-length'));
            //focus on first input of cloned element
            $clone.find('input:visible:first').focus();
            $clone.find(' .intel-alert').addClass('hidden');
            if ($('.multi-fields-group').length === limit) {
                addLink.hide();
                $('.add-hint').show();
            } else {
                $('.add-hint').hide();
                addLink.show();
            }
        }

        function hideTogglingElementsInClone($clone) {
            //hide the data-hidden-by elements in the clone
            $clone.find('*[data-hidden-by]').closest('.govuk-form-group').addClass('hidden');
            $clone.find('*[data-hides-this]').addClass('hidden');
        }
        function removeValuesFromClonedInputs($clone) {
            $clone.find(':not(:radio):not(.keep-val)').val('');
            $clone.find(':radio').removeAttr('checked');
            $clone.find('textarea').val('');
        }
        function removeErrors($clone) {
            //GDS error classes to remove from cloned element
            $clone.find('.form-grouped-error').removeClass('form-grouped-error');
            $clone.find('.govuk-error-message').remove();
            $clone.find('input.govuk-input--error').removeClass('govuk-input--error');
            $clone.find('.govuk-form-group').removeClass('govuk-form-group--error');
            $clone.removeClass('form-grouped-error');
            $clone.find('.form-group').removeClass('has-error');
        }


    };

    VoaFor.addMultiButtonState = function () {
        var $element = $('.add-multi-fields');
        var $group = $('.multi-fields-group');
        var l = parseInt($element.closest('fieldset').attr('data-limit'), 10);
        if ($group.length === l) {
            $element.hide();
        } else {
            $element.show();
        }
    };

    VoaFor.removeFieldMulti = function () {
        $(document).on('click', '.remove-multi-fields', function (e) {
            e.preventDefault();
            var limit = parseInt($(this).closest('fieldset').attr('data-limit'), 10);
            $(this).closest('.multi-fields-group').remove();
            $('.add-hint').hide();
            if ($('.multi-fields-group').length === 1) {
                $('.multi-fields-group:first').find('.remove-multi-fields').css('display', 'none');
            }
            if ($('.multi-fields-group').length < limit) {
                $('.add-multi-fields').show();
            } else {
                $('.add-multi-fields').hide();
            }
            $('.multi-fields-group').each(function (i) {
                VoaFor.changeIds(this, i);
            });
        });

        var fieldsetMin = $('fieldset[data-min-size]');
        if (fieldsetMin) {
            var minSize = parseInt(fieldsetMin.attr('data-min-size'), 10);
            $('.multi-fields-group').find('.remove-multi-fields').slice(minSize).css('display', 'inline-block');
        } else {
            $('.multi-fields-group:not(:first)').find('.remove-multi-fields').css('display', 'inline-block');
        }
    };

    VoaFor.selectMobile = function () {
        function setSelectSize() {
            if ($(window).width() <= 640) {
                $('.addressList').attr('size', '');
            } else {
                $('.addressList').attr('size', '8');
            }
        }

        $(window).resize(function () {
            setSelectSize();
        });
        setSelectSize();
    };

    VoaFor.isEdit = function () {
        if (VoaCommon.getQueryString('edit')) {
            $('[name="continue_button"]').text(VoaMessages.textLabel('buttonUpdate')).attr('name', 'update_button');
        }
    };

    VoaFor.doYouOwnTheProperty = function(){
        $(':radio[name=propertyOwnedByYou]').on('click', function (){
            $(':radio[name=propertyRentedByYou]').removeAttr('checked');
            $('textarea#noRentDetails').removeClass('govuk-textarea--error');
            var enclosingFormGroupEl = $('textarea#noRentDetails').closest('.govuk-form-group--error');
            enclosingFormGroupEl.removeClass('govuk-form-group--error');
            enclosingFormGroupEl.find('.govuk-error-message').remove();
        });
    };

    VoaFor.getReferrer = function () {
        var s = window.location.href, i = s.indexOf('?') + 1, r, v;

        if (ref) {
            r = ref.replace(/\//g, '');
        }

        if (i > 0) {
            v = [s.slice(0, i), 'id=' + r + '&', s.slice(i)].join('');
        } else {
            v = s + '?id=' + r;
        }

        $('#footerBetaFeedback, #betaFeedback').each(function (e) {
            $(this).attr('href', $(this).attr('href') + '?page=' + window.location.href.substr(window.location.href.lastIndexOf('/') + 1).replace(/\?/g, '&'));
        });

        $('.form--feedback [name="referrer"], .form--feedback #referer').val(v);
    };

})(jQuery);
