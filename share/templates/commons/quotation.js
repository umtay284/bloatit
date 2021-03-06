function round2(value) {
  return parseFloat((value).toFixed(2));
}

function QuotationTotalEntry(id) {
    this.id = id
    this.entries = new Array();
    
    this.addEntry = function(entry) {
        this.entries.push(entry);
        return this;
    }

    this.getValue = function() {
        var value = 0;
        for(var i= 0; i < this.entries.length; i++) {
            value = value + this.entries[i].getValue()
        }
        return round2(value);
    }
}

function Quotation(id, total) {
    this.id = id
  
    this.total = total

    this.getValue = function() {
        return round2(this.total);
    }
}

function QuotationAmountEntry(id, amount) {
    this.id = id
  
    this.amount = amount

    this.getValue = function() {
        return round2(this.amount);
    }
}

function QuotationProxyEntry(id, reference) {
    this.id = id
  
    
    this.reference = reference

    this.getValue = function() {
        return round2(this.reference.getValue());
    }
}

function QuotationPercentEntry(id, reference, percent) {
    this.id = id
       
    this.percent = percent
    this.reference = reference

    this.getValue = function() {
        return round2(this.reference.getValue() * percent);
    }
}

function QuotationMultiplyEntry(id, reference, percent) {
    this.id = id
       
    this.percent = percent
    this.reference = reference

    this.getValue = function() {
        return this.reference.getValue() * percent;
    }
}

function QuotationDifferenceEntry(id, reference1, reference2) {
    this.id = id
       
    this.reference1 = reference1
    this.reference2 = reference2

    this.getValue = function() {
        return round2(this.reference1.getValue() - this.reference2.getValue());
    }
}

//Settings
var chargeField = "${charge_field_id}";
var pretotal = ${pre_total};
var callbackUrl = "${callback_url}";
var commissionVariableRate = ${commission_variable_rate};
var commissionFixRate = ${commission_fix_rate};
var inputOffset = ${input_offset};
var outputOffset = ${output_offset};
var locale = ${locale};

quotationEntries = new Array();

${entry_list}

function update() {
    var newValue = parseFloat($( "#"+chargeField ).val());

    if(isNaN(newValue)) {
        return
    }


    var baseValueText = $( "#input_money" ).html()
    var baseValueTextFormated = baseValueText;
    baseValueTextFormated = baseValueTextFormated.replace("&nbsp;","");
    baseValueTextFormated = baseValueTextFormated.replace(",","");
    var baseValue = parseFloat(baseValueTextFormated);

    var amount = pretotal + newValue;
    quotationEntries[inputOffset].amount = amount;
    quotationEntries[outputOffset].total =  amount + amount * commissionVariableRate + commissionFixRate;

    for(var i= 0; i < quotationEntries.length; i++) {
        quotationEntry = quotationEntries[i];
        $( "#"+quotationEntry.id ).html(quotationEntry.getValue().toFixed(2) + "&nbsp;€");
    }

    $("#output_money").html(addNumberSeparator((newValue + baseValue).toFixed(0)) + "&nbsp;€")

    //Update process
    var http = new XMLHttpRequest();
    http.open('GET', callbackUrl+"?silent=true&preload="+newValue, true);
    http.send(null);
};

function addNumberSeparator(numberStr)
{
	numberStr += '';
	sep = (locale == "fr" ? '&nbsp;' : ',');
	var rgx = /(\d+)(\d{3})/;
	while (rgx.test(numberStr)) {
		numberStr = numberStr.replace(rgx, '$1' + sep + '$2');
	}
	return numberStr;
}


//$( "#"+chargeField).keyup(update());
//$( "#"+chargeField).change(update());

$( "#"+chargeField).bind('input', update);
