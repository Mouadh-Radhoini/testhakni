import { Pipe, PipeTransform } from '@angular/core';
import { currentLanguage, Lang } from './i18n';

/**
 * Traduction des messages renvoyes par le serveur (en francais) : erreurs metier, erreurs de
 * validation par champ et notifications. Le francais est renvoye tel quel ; un message inconnu
 * n'est jamais perdu (il reste affiche en francais).
 */
const EXACT: Record<string, [string, string]> = {
  'Donnees invalides': ['Invalid data', 'بيانات غير صالحة'],
  'Un service a reçu 3 signalements ouverts : à examiner.': ['A service has received 3 open reports: please review.', 'تلقّت خدمة 3 بلاغات مفتوحة: يرجى المراجعة.'],
  'Un avis a reçu 3 signalements ouverts : à examiner.': ['A review has received 3 open reports: please review.', 'تلقّى تقييم 3 بلاغات مفتوحة: يرجى المراجعة.'],
  'Email ou mot de passe incorrect': ['Incorrect email or password', 'البريد الإلكتروني أو كلمة المرور غير صحيحة'],
  'Refresh token invalide ou expire': ['Session expired, please log in again', 'انتهت الجلسة، يرجى تسجيل الدخول من جديد'],
  'Compte suspendu. Contactez l\'administrateur.': ['Account suspended. Contact the administrator.', 'الحساب معلّق. تواصل مع المشرف.'],
  'Un compte existe deja avec cet email': ['An account already exists with this email', 'يوجد حساب بهذا البريد الإلكتروني'],
  'Mot de passe actuel incorrect': ['Current password is incorrect', 'كلمة المرور الحالية غير صحيحة'],
  'Authentification requise': ['Authentication required', 'يلزم تسجيل الدخول'],
  'Token invalide ou expire': ['Invalid or expired session', 'جلسة غير صالحة أو منتهية'],
  'Acces refuse': ['Access denied', 'الوصول مرفوض'],
  'Acces refuse pour votre role': ['Access denied for your role', 'الوصول مرفوض لدورك'],
  'Erreur interne du serveur': ['Internal server error', 'خطأ داخلي في الخادم'],
  'Ressource introuvable': ['Resource not found', 'المورد غير موجود'],
  'Cette ressource existe deja': ['This resource already exists', 'هذا المورد موجود مسبقًا'],
  'Modification concurrente, actualisez la page': ['Concurrent change, please refresh the page', 'تعديل متزامن، يرجى تحديث الصفحة'],
  'Corps de requete JSON invalide ou valeur non reconnue': ['Invalid request body or unrecognised value', 'محتوى الطلب غير صالح أو قيمة غير معروفة'],
  'Type de contenu non pris en charge': ['Unsupported content type', 'نوع المحتوى غير مدعوم'],
  'Methode HTTP non supportee': ['HTTP method not supported', 'طريقة HTTP غير مدعومة'],
  'Fichier trop lourd (2 Mo maximum)': ['File too large (2 MB maximum)', 'الملف كبير جدًا (2 ميغابايت كحد أقصى)'],
  'Image trop lourde (2 Mo maximum)': ['Image too large (2 MB maximum)', 'الصورة كبيرة جدًا (2 ميغابايت كحد أقصى)'],
  'Format non accepte : JPEG, PNG ou WebP uniquement': ['Unsupported format: JPEG, PNG or WebP only', 'صيغة غير مقبولة: JPEG أو PNG أو WebP فقط'],
  'Fichier vide': ['Empty file', 'ملف فارغ'],
  'Image introuvable': ['Image not found', 'الصورة غير موجودة'],
  'Photo introuvable': ['Photo not found', 'الصورة غير موجودة'],
  'Trop de tentatives de connexion pour ce compte. Réessayez dans quelques minutes.': ['Too many login attempts for this account. Try again in a few minutes.', 'محاولات دخول كثيرة لهذا الحساب. أعد المحاولة بعد بضع دقائق.'],
  'Connectez-vous pour filtrer ou trier par prix': ['Log in to filter or sort by price', 'سجّل الدخول للتصفية أو الترتيب حسب السعر'],
  'Le prix minimum doit etre inferieur ou egal au prix maximum': ['The minimum price must not exceed the maximum price', 'يجب ألا يتجاوز السعر الأدنى السعر الأقصى'],
  'Service introuvable': ['Service not found', 'الخدمة غير موجودة'],
  'Ce service ne vous appartient pas': ['This service does not belong to you', 'هذه الخدمة ليست لك'],
  'Service suspendu par un administrateur': ['Service suspended by an administrator', 'الخدمة معلّقة من طرف مشرف'],
  'Statut autorise : ACTIVE ou INACTIVE': ['Allowed status: ACTIVE or INACTIVE', 'الحالات المسموحة: نشط أو معطّل'],
  'Le service indique n\'existe pas ou n\'est plus disponible': ['The selected service does not exist or is no longer available', 'الخدمة المحددة غير موجودة أو لم تعد متاحة'],
  'Demande introuvable': ['Request not found', 'الطلب غير موجود'],
  'Cette demande ne vous appartient pas': ['This request does not belong to you', 'هذا الطلب ليس لك'],
  'Seule une demande ouverte peut etre annulee': ['Only an open request can be cancelled', 'لا يمكن إلغاء إلا الطلب المفتوح'],
  'Demande privée': ['Private request', 'طلب خاص'],
  'Demande clôturée': ['Request closed', 'الطلب مغلق'],
  'Demande déjà traitée': ['Request already processed', 'تمت معالجة الطلب مسبقًا'],
  'Demande indisponible ou hors de votre zone': ['Request unavailable or outside your area', 'الطلب غير متاح أو خارج منطقتك'],
  'Profil indisponible': ['Profile unavailable', 'الملف غير متاح'],
  'Profil introuvable': ['Profile not found', 'الملف غير موجود'],
  'Profil prestataire pas encore cree': ['Provider profile not created yet', 'لم يتم إنشاء ملف مقدّم الخدمة بعد'],
  'Prestataire introuvable': ['Provider not found', 'مقدّم الخدمة غير موجود'],
  'Offre introuvable': ['Offer not found', 'العرض غير موجود'],
  'Offre privée': ['Private offer', 'عرض خاص'],
  'Offre déjà traitée': ['Offer already processed', 'تمت معالجة العرض مسبقًا'],
  'Offre expirée ou prestataire suspendu': ['Offer expired or provider suspended', 'العرض منتهٍ أو مقدّم الخدمة معلّق'],
  'Réservation introuvable': ['Booking not found', 'الحجز غير موجود'],
  'Réservation privée': ['Private booking', 'حجز خاص'],
  'Action réservée au client': ['Action reserved to the customer', 'الإجراء مخصّص للحريف'],
  'Action réservée à l’autre participant': ['Action reserved to the other participant', 'الإجراء مخصّص للطرف الآخر'],
  'Action impossible depuis le statut actuel': ['Not possible from the current status', 'الإجراء غير ممكن من الحالة الحالية'],
  'Transition impossible': ['Transition not possible', 'الانتقال غير ممكن'],
  'Transition impossible depuis le statut actuel': ['Not possible from the current status', 'الإجراء غير ممكن من الحالة الحالية'],
  'Statut autorisé : IN_PROGRESS, COMPLETED ou CANCELLED': ['Allowed status: IN_PROGRESS, COMPLETED or CANCELLED', 'الحالات المسموحة: قيد التنفيذ أو منتهية أو ملغاة'],
  'État de la demande incompatible': ['Incompatible request status', 'حالة الطلب غير متوافقة'],
  'Fin de prestation non confirmée': ['End of the job not confirmed', 'لم يتم تأكيد انتهاء الخدمة'],
  'Avis réservé au client': ['Review reserved to the customer', 'التقييم مخصّص للحريف'],
  'Avis introuvable': ['Review not found', 'التقييم غير موجود'],
  'Notification introuvable': ['Notification not found', 'الإشعار غير موجود'],
  'Utilisateur introuvable': ['User not found', 'المستخدم غير موجود'],
  'Compte administrateur protégé': ['Protected administrator account', 'حساب المشرف محمي'],
  'Choisissez une décision finale': ['Choose a final decision', 'اختر قرارًا نهائيًا'],
  'Signalement déjà traité': ['Report already processed', 'تمت معالجة البلاغ مسبقًا'],
  'Contact introuvable': ['Contact not found', 'جهة الاتصال غير موجودة'],
  'Contact privé': ['Private contact', 'جهة اتصال خاصة'],
  'Ce contact a été retiré': ['This contact has been removed', 'تمت إزالة جهة الاتصال هذه'],
  'Seul le destinataire peut répondre': ['Only the recipient can respond', 'المستلم وحده يمكنه الرد'],
  'Vous ne pouvez pas vous ajouter vous-même': ['You cannot add yourself', 'لا يمكنك إضافة نفسك'],
  'Les contacts relient un client et un prestataire': ['Contacts link a customer and a provider', 'تربط جهات الاتصال بين حريف ومقدّم خدمة'],
  'Ajoutez d\'abord cette personne à vos contacts': ['Add this person to your contacts first', 'أضف هذا الشخص إلى جهات اتصالك أولًا'],
  'Conversation introuvable': ['Conversation not found', 'المحادثة غير موجودة'],
  'Conversation privée': ['Private conversation', 'محادثة خاصة'],
  'Conversation réservée au client et au prestataire': ['Conversation reserved to the customer and the provider', 'المحادثة مخصّصة للحريف ومقدّم الخدمة'],
  'Le message est vide': ['The message is empty', 'الرسالة فارغة'],
  '2000 caracteres maximum': ['2000 characters maximum', '2000 حرف كحد أقصى'],
  '1000 caracteres maximum': ['1000 characters maximum', '1000 حرف كحد أقصى'],
  '2 decimales maximum': ['2 decimals maximum', 'خانتان عشريتان كحد أقصى'],
  '20 competences maximum': ['20 skills maximum', '20 مهارة كحد أقصى'],
  '50 maximum': ['50 maximum', '50 كحد أقصى'],
  '60 ans maximum': ['60 years maximum', '60 سنة كحد أقصى'],
  'Budget trop eleve': ['Budget too high', 'الميزانية مرتفعة جدًا'],
  'Prix trop eleve': ['Price too high', 'السعر مرتفع جدًا'],
  'Choisissez au moins une categorie': ['Choose at least one category', 'اختر صنفًا واحدًا على الأقل'],
  'Destinataire obligatoire': ['Recipient required', 'المستلم إلزامي'],
  'Doit etre >= 0': ['Must be >= 0', 'يجب أن تكون ≥ 0'],
  'Doit etre >= 1': ['Must be >= 1', 'يجب أن تكون ≥ 1'],
  'Doit etre positif': ['Must be positive', 'يجب أن تكون موجبة'],
  'Email invalide': ['Invalid email', 'بريد إلكتروني غير صالح'],
  'L\'email est obligatoire': ['Email is required', 'البريد الإلكتروني إلزامي'],
  'La categorie est obligatoire': ['Category is required', 'الصنف إلزامي'],
  'La date souhaitee ne peut pas etre dans le passe': ['The preferred date cannot be in the past', 'لا يمكن أن يكون التاريخ المطلوب في الماضي'],
  'La description doit faire entre 10 et 2000 caracteres': ['The description must be between 10 and 2000 characters', 'يجب أن يتراوح الوصف بين 10 و2000 حرف'],
  'La description est obligatoire': ['Description is required', 'الوصف إلزامي'],
  'La ville est obligatoire pour un prestataire': ['City is required for a provider', 'المدينة إلزامية لمقدّم الخدمة'],
  'La ville est obligatoire': ['City is required', 'المدينة إلزامية'],
  'Le budget doit etre positif': ['The budget must be positive', 'يجب أن تكون الميزانية موجبة'],
  'Le mot de passe actuel est obligatoire': ['Current password is required', 'كلمة المرور الحالية إلزامية'],
  'Le mot de passe est obligatoire': ['Password is required', 'كلمة المرور إلزامية'],
  'Le nom commercial est obligatoire': ['Business name is required', 'الاسم التجاري إلزامي'],
  'Le nom est obligatoire': ['Last name is required', 'اللقب إلزامي'],
  'Le nouveau mot de passe est obligatoire': ['New password is required', 'كلمة المرور الجديدة إلزامية'],
  'Le prenom est obligatoire': ['First name is required', 'الاسم إلزامي'],
  'Le prix doit etre positif': ['The price must be positive', 'يجب أن يكون السعر موجبًا'],
  'Le prix est obligatoire sauf pour un service sur devis': ['A price is required unless the service is on quote', 'السعر إلزامي إلا للخدمة حسب التقدير'],
  'Le refresh token est obligatoire': ['Session token is required', 'رمز الجلسة إلزامي'],
  'Le statut est obligatoire': ['Status is required', 'الحالة إلزامية'],
  'Le telephone est obligatoire pour un prestataire': ['Phone is required for a provider', 'الهاتف إلزامي لمقدّم الخدمة'],
  'Le titre doit faire entre 3 et 100 caracteres': ['The title must be between 3 and 100 characters', 'يجب أن يتراوح العنوان بين 3 و100 حرف'],
  'Le titre est obligatoire': ['Title is required', 'العنوان إلزامي'],
  'Le type de prix est obligatoire': ['Price type is required', 'نوع السعر إلزامي'],
  'Numero de telephone invalide': ['Invalid phone number', 'رقم هاتف غير صالح'],
  'Le mot de passe doit contenir 8 a 72 caracteres, dont une majuscule, une minuscule, un chiffre et un caractere special': ['The password must contain 8 to 72 characters, including an uppercase letter, a lowercase letter, a digit and a special character', 'يجب أن تحتوي كلمة المرور على 8 إلى 72 حرفًا، منها حرف كبير وحرف صغير ورقم ورمز خاص'],
  'Vous avez reçu un avis': ['You received a review', 'وصلك تقييم'],
  'Nouvelle demande de contact': ['New contact request', 'طلب تواصل جديد'],
  'Votre demande de contact a été acceptée': ['Your contact request was accepted', 'تم قبول طلب التواصل الخاص بك'],
  'Une offre a été retirée': ['An offer was withdrawn', 'تم سحب عرض'],
  'Votre offre a été acceptée': ['Your offer was accepted', 'تم قبول عرضك'],
  'Une autre offre a été retenue': ['Another offer was chosen', 'تم اختيار عرض آخر'],
  'La demande a été annulée': ['The request was cancelled', 'تم إلغاء الطلب'],
};

/** Messages dont la fin est variable (titre, motif...) : prefixe francais -> prefixes traduits. */
const PREFIXES: [string, string, string][] = [
  ['Nouvelle offre pour ', 'New offer for ', 'عرض جديد على '],
  ['Décision de modération : ', 'Moderation decision: ', 'قرار الإشراف: '],
  ['Réservation mise à jour : ', 'Booking updated: ', 'تم تحديث الحجز: '],
  ['Le client a confirmé la fin de la prestation : ', 'The customer confirmed the job is done: ', 'أكّد الحريف انتهاء الخدمة: '],
  ['Le client a annulé la réservation : ', 'The customer cancelled the booking: ', 'ألغى الحريف الحجز: '],
  ['Le client indique que le travail a commencé : ', 'The customer says the work has started: ', 'أفاد الحريف بأن العمل قد بدأ: '],
];

const RETRY = /^Trop de tentatives\. Réessayez dans (\d+) secondes\.$/;

export function serverText(message: string | null | undefined, lang: Lang): string {
  if (!message || lang === 'fr') return message ?? '';
  const index = lang === 'en' ? 0 : 1;
  const exact = EXACT[message];
  if (exact) return exact[index];
  for (const [fr, en, ar] of PREFIXES) {
    if (message.startsWith(fr)) return (lang === 'en' ? en : ar) + message.slice(fr.length);
  }
  const retry = RETRY.exec(message);
  if (retry) {
    return lang === 'en'
      ? `Too many attempts. Try again in ${retry[1]} seconds.`
      : `محاولات كثيرة. أعد المحاولة بعد ${retry[1]} ثانية.`;
  }
  return message;
}

/** Raccourci : traduit dans la langue courante de l'interface. */
export function serverMessage(message: string | null | undefined): string {
  return serverText(message, currentLanguage());
}

/** {{ message | serverText }} : traduit un message du serveur, et suit le changement de langue. */
@Pipe({ name: 'serverText', pure: false })
export class ServerTextPipe implements PipeTransform {
  transform(message: string | null | undefined): string {
    return serverMessage(message);
  }
}
