import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import ModalLayout from '../components/layout/ModalLayout';
import AuthLayout from '../components/layout/AuthLayout';

const INITIAL_STATE = { email: '' };
const authBackgroundStyle = { width: '100%', maxWidth: '450px', minHeight: '450px' };

export default function ForgotPassword() {
    const [formData, setFormData] = useState(INITIAL_STATE);
    const [modal, setModal] = useState({ show: false, title: '', message: '', isError: false, onConfirm: null });
    const navigate = useNavigate();

    const resetForm = () => setFormData(INITIAL_STATE);

    const handleChange = (e) => {
        setFormData({ ...formData, [e.target.name]: e.target.value });
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        try {
            const { data, status } = await axios.post(`${process.env.REACT_APP_API_URL}/user/forgot/password`, formData);
            if (status === 200) {
                setModal({
                    show: true,
                    title: 'Success',
                    message: data.message,
                    isError: false,
                    onConfirm: () => {
                        setModal({ ...modal, show: false });
                        navigate('/otp/verify', { state: { email: formData.email } });
                    }
                });
                resetForm();
            }
        } catch (err) {
            const errorMessage = err.response.data?.error || 'Forgot Password failed.';
            setModal({
                show: true,
                title: 'Failed',
                message: <span className='text-danger'>{errorMessage}</span>,
                isError: true,
                onConfirm: () => setModal({ ...modal, show: false }),
            });
        }
    };

    return (
        <>
            <AuthLayout customStyle={authBackgroundStyle}>

                <h3 className='mb-3'>Forgot Password</h3>

                <p className='text-muted mb-3'>Please enter your registered email to reset your password.</p>

                {/* Form */}
                <form onSubmit={handleSubmit}>
                    <div className='mb-3'>
                        <label className='text-6c757d' htmlFor='email'>Email</label>
                        <input className='form-control' type='email' id='email' name='email' value={formData.email} onChange={handleChange} autoComplete='email' required />
                    </div>

                    <div className='mb-3'>
                        <button type='submit' className='btn btn-dark w-100'>Get OTP</button>
                    </div>
                </form>

            </AuthLayout>

            {/* Alert Modal */}
            {modal.show && (
                <ModalLayout title={modal.title} msg={modal.message} modal={modal.onConfirm} hideClose={!modal.isError}>
                    <button onClick={modal.onConfirm} className={`btn btn-dark px-3`}>
                        {modal.isError ? 'Try Again' : 'OK'}
                    </button>
                </ModalLayout>
            )}
        </>
    );
};