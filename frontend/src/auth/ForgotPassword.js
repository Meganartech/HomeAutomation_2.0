import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import AuthBackground from '../components/layout/AuthLayout';

const INITIAL_STATE = { email: '' };

export default function ForgotPassword() {

    const [formData, setFormData] = useState(INITIAL_STATE);
    const navigate = useNavigate();

    const resetForm = () => setFormData(INITIAL_STATE);

    const handleChange = (e) => {
        setFormData({ ...formData, [e.target.name]: e.target.value });
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        try {
            const { data, status } = await axios.post('http://localhost:8081/user/forgot/password', formData);
            if (status === 200) {
                alert(data.message);
                navigate('/otp/verify', { state: { email: formData.email } });
            }
        } catch (err) {
            const error = err.data?.error || 'Forgot Password Failed';
            console.error(error);
        } finally {
            resetForm();
        }
    };

    const customStyle = { width: '100%', maxWidth: '450px', minHeight: '450px' };

    return (
        <>
            <AuthBackground customStyle={customStyle}>

                <h3 className='mb-3'>Forgot Password</h3>

                <p className='text-muted mb-3'>Please enter your registered email to reset your password.</p>

                <form onSubmit={handleSubmit}>
                    <div className='mb-3'>
                        <label className='text-6c757d' htmlFor='email'>Email</label>
                        <input className='form-control' type='email' id='email' name='email' value={formData.email} onChange={handleChange} autoComplete='email' required />
                    </div>

                    <div className='mb-3'>
                        <button type='submit' className='btn btn-dark w-100'>Get OTP</button>
                    </div>
                </form>

            </AuthBackground>
        </>
    );
};